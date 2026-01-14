package com.phew.feed.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.phew.core.ui.model.navigation.CardDetailArgs
import com.phew.core_common.CardDetailTrace
import com.phew.domain.dto.FeedData
import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.log.SooumLog
import com.phew.domain.dto.DistanceCard
import com.phew.domain.dto.FeedCardType
import com.phew.domain.dto.Latest
import com.phew.domain.dto.Location
import com.phew.domain.dto.Notice
import com.phew.domain.dto.NoticeSource
import com.phew.domain.dto.Notification
import com.phew.domain.dto.Notify
import com.phew.domain.dto.Popular
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.network.CardFeedRepository
import com.phew.domain.usecase.CheckCardAlreadyDelete
import com.phew.domain.usecase.CheckLocationPermission
import com.phew.domain.usecase.GetFeedNotification
import com.phew.domain.usecase.GetLatestFeed
import com.phew.domain.usecase.GetNotification
import com.phew.domain.usecase.GetReadNotification
import com.phew.domain.usecase.GetUnReadNotification
import com.phew.domain.usecase.SaveEventLogFeedView
import com.phew.domain.usecase.SetReadActivateNotify
import com.phew.domain.usecase.SetReadActivateNotify.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.collections.emptyList

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val locationAsk: CheckLocationPermission,
    getNotificationPage: GetNotification,
    getUnReadNotification: GetUnReadNotification,
    getReadNotification: GetReadNotification,
    private val getLatestFeed: GetLatestFeed,
    private val cardFeedRepository: CardFeedRepository,
    private val deviceRepository: DeviceRepository,
    private val notification: GetFeedNotification,
    private val readNotify: SetReadActivateNotify,
    private val checkCardDelete: CheckCardAlreadyDelete,
    private val eventLog: SaveEventLogFeedView,
) :
    ViewModel() {
    private val _uiState = MutableStateFlow(Home())
    val uiState: StateFlow<Home> = _uiState.asStateFlow()

    // Navigation side effects
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    // 알람 읽음 처리를 위해
    private val mutex = Mutex()
    private val unreadIdBuffer = mutableSetOf<Long>()

    /**
     * 공지사항(notice)
     * 활동알림(unRead , read)
     */

    init {
        // 병렬 초기화: 독립적인 작업들을 동시에 실행
        viewModelScope.launch {
            // 병렬로 실행할 작업들
            launch { loadInitialFeeds() }     // 위치 설정
            launch { 
                // 초기 로딩 완료 후 탭 변경 감지 시작
                startTabChangeListener()
            }
            launch { getFeedNotice() }        // 피드 노티스 로딩 (초기 1회만)
        }
    }

    private suspend fun startTabChangeListener() {
        // 탭 변경 감지하여 필요시 데이터 로딩
        uiState.map { it.currentTab }
            .distinctUntilChanged()
            .drop(1) // 초기값 무시
            .collect { currentTab ->
                when (currentTab) {
                    FeedType.Latest -> {
                        if (_uiState.value.latestPagingState is FeedPagingState.None) {
                            _uiState.update { it.copy(latestPagingState = FeedPagingState.Loading) }
                            loadLatestFeeds(isInitial = true)
                        }
                    }

                    FeedType.Popular -> {
                        if (_uiState.value.popularPagingState is FeedPagingState.None) {
                            _uiState.update { it.copy(popularPagingState = FeedPagingState.Loading) }
                            loadPopularFeeds(isInitial = true)
                        }
                    }

                    FeedType.Distance -> {
                        val currentDistanceTab = _uiState.value.distanceTab
                        val currentStateForTab =
                            _uiState.value.distancePagingStates[currentDistanceTab]
                        if (currentStateForTab == null || currentStateForTab is FeedPagingState.None) {
                            _uiState.update { state ->
                                val newStates = state.distancePagingStates.toMutableMap()
                                newStates[currentDistanceTab] = FeedPagingState.Loading
                                state.copy(distancePagingStates = newStates)
                            }
                            loadDistanceFeeds(isInitial = true)
                        }
                    }
                }
            }
    }

    private suspend fun loadInitialFeeds() {
        // Location 초기 설정
        val location = getLocationSafely()
        _latestFeedLocation.value = LatestFeedQuery(
            latitude = location.latitude.takeIf { it != 0.0 },
            longitude = location.longitude.takeIf { it != 0.0 }
        )
    }

    val notice: Flow<PagingData<Notice>> =
        getNotificationPage(NoticeSource.NOTIFICATION).cachedIn(viewModelScope)
    val unReadActivateAlarm: Flow<PagingData<Notification>> =
        getUnReadNotification().cachedIn(viewModelScope)
    val readActivateAlarm: Flow<PagingData<Notification>> =
        getReadNotification().cachedIn(viewModelScope)

    // Latest Feed Paging
    /**
     * 최신 피드 페이징은 위치가 동일해도 새로고침 시 스트림을 다시 구독해야 하므로
     * refreshToken을 함께 사용해 강제로 플로우를 재시작한다.
     */
    private data class LatestFeedQuery(
        val latitude: Double?,
        val longitude: Double?,
        val refreshToken: Long = 0L,
    )

    private val _latestFeedLocation =
        MutableStateFlow(LatestFeedQuery(latitude = null, longitude = null))

    private fun triggerLatestFeedRefresh() {
        _latestFeedLocation.update { current ->
            current.copy(refreshToken = current.refreshToken + 1)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val latestFeedPaging: Flow<PagingData<Latest>> = _latestFeedLocation
        .flatMapLatest { query ->
            getLatestFeed(query.latitude, query.longitude)
        }
        .cachedIn(viewModelScope)

    /**
     * 권한 요청
     */
    private val _requestPermissionEvent = MutableSharedFlow<Array<String>>()
    val requestPermissionEvent = _requestPermissionEvent.asSharedFlow()

    fun checkLocationPermission() {
        val isGranted = locationAsk()
        if (isGranted) {
            getLocation()
            return
        }
        _uiState.update { state ->
            state.copy(shouldShowPermissionRationale = true)
        }
    }

    fun clickHomeTab() {
        viewModelScope.launch(Dispatchers.IO) {
            eventLog.moveToTop()
        }
    }

    fun logMoveCardDetail() {
        viewModelScope.launch(Dispatchers.IO) {
            eventLog.moveToCardDetail()
        }
    }

    private suspend fun getLocationSafely(): Location {
        return try {
            deviceRepository.requestLocation()
        } catch (e: Exception) {
            // 위치 정보 가져오기 실패 시 빈 위치 반환
            e.printStackTrace()
            Location.EMPTY
        }
    }

    private fun getLocation() {
        viewModelScope.launch {
            val location = getLocationSafely()
            _uiState.update { state ->
                state.copy(location = location, currentTab = FeedType.Distance)
            }
            // Latest feed location도 업데이트
            _latestFeedLocation.value = LatestFeedQuery(
                latitude = location.latitude.takeIf { it != 0.0 },
                longitude = location.longitude.takeIf { it != 0.0 }
            )
        }
    }

    private fun getFeedNotice() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val request = notification(NoticeSource.NOTIFICATION)) {
                is DomainResult.Failure -> {
                    _uiState.update { state -> state.copy(feedNotification = UiState.Fail(request.error)) }
                }

                is DomainResult.Success -> {
                    _uiState.update { state -> state.copy(feedNotification = UiState.Success(request.data)) }
                }
            }
        }
    }

    fun onPermissionRequest(permission: Array<String>) {
        viewModelScope.launch {
            _requestPermissionEvent.emit(permission)
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        if (!isGranted) {
            _uiState.update { state ->
                state.copy(shouldShowPermissionRationale = true)
            }
        }
    }

    fun rationalDialogDismissed() {
        _uiState.update { state ->
            state.copy(shouldShowPermissionRationale = false)
        }
    }

    // TODO 개선 작업 필요 포인트
    private fun loadLatestFeeds(isInitial: Boolean) {
        viewModelScope.launch {
            val currentState = _uiState.value.latestPagingState

            if (!isInitial) {
                if (currentState !is FeedPagingState.Success || !currentState.hasNextPage) {
                    return@launch
                }
            }

            val existingCards = if (isInitial || currentState !is FeedPagingState.Success) {
                emptyList()
            } else {
                currentState.feedCards
            }
            _uiState.update {
                it.copy(
                    latestPagingState = if (isInitial) FeedPagingState.Loading else FeedPagingState.LoadingMore(
                        existingCards
                    )
                )
            }

            val lastId = if (isInitial) null else (currentState as? FeedPagingState.Success)?.lastId

            try {
                val location = getLocationSafely()
                val latitude = location.latitude.takeIf { it != 0.0 }
                val longitude = location.longitude.takeIf { it != 0.0 }

                when (val result = cardFeedRepository.requestFeedLatest(
                    latitude = latitude,
                    longitude = longitude,
                    lastId = lastId
                )) {
                    is DataResult.Success -> {
                        val newFeedCards = mapLatestToFeedCards(result.data)
                        val isDuplicate =
                            newFeedCards.isNotEmpty() && newFeedCards == existingCards.takeLast(
                                newFeedCards.size
                            )
                        SooumLog.d(
                            TAG,
                            "Latest feed duplicate check: $isDuplicate (new=${newFeedCards.size}, existing=${existingCards.size})"
                        )
                        _uiState.update { state ->
                            state.copy(
                                location = location,
                                latestPagingState = FeedPagingState.Success(
                                    feedCards = existingCards + newFeedCards,
                                    hasNextPage = result.data.isNotEmpty(),
                                    lastId = result.data.lastOrNull()?.cardId?.toLongOrNull()
                                ),
                            )
                        }
                    }

                    is DataResult.Fail -> {
                        _uiState.update {
                            it.copy(
                                latestPagingState = FeedPagingState.Error(
                                    result.message ?: "최신 피드 로딩 실패"
                                ),
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        latestPagingState = FeedPagingState.Error(
                            e.message ?: "최신 피드 로딩 실패"
                        )
                    )
                }
            }
        }
    }

    private fun loadPopularFeeds(isInitial: Boolean) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value.popularPagingState

                if (!isInitial) {
                    val existingCards =
                        (currentState as? FeedPagingState.Success)?.feedCards ?: emptyList()
                    _uiState.update {
                        it.copy(popularPagingState = FeedPagingState.LoadingMore(existingCards))
                    }
                }

                val location = getLocationSafely()
                val latitude = location.latitude.takeIf { it != 0.0 }
                val longitude = location.longitude.takeIf { it != 0.0 }

                when (val result = cardFeedRepository.requestFeedPopular(
                    latitude = latitude,
                    longitude = longitude
                )) {
                    is DataResult.Success -> {
                        val newFeedCards = mapPopularToFeedCards(result.data)
                        val existingCards = if (isInitial) emptyList() else {
                            (currentState as? FeedPagingState.Success)?.feedCards ?: emptyList()
                        }
                        _uiState.update { state ->
                            state.copy(
                                location = location,
                                popularPagingState = FeedPagingState.Success(
                                    feedCards = existingCards + newFeedCards,
                                    hasNextPage = false, // Popular는 페이징이 없음
                                    lastId = null // Popular는 페이징이 없으므로 null
                                ),
                                refresh = false //요기 수정
                            )
                        }
                    }

                    is DataResult.Fail -> {
                        _uiState.update {
                            it.copy(
                                popularPagingState = FeedPagingState.Error(
                                    result.message ?: "인기 피드 로딩 실패"
                                ),
                                refresh = false //요기 수정
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        popularPagingState = FeedPagingState.Error(
                            e.message ?: "인기 피드 로딩 실패"
                        )
                    )
                }
            }
        }
    }

    private fun loadDistanceFeeds(isInitial: Boolean) {
        viewModelScope.launch {
            val currentDistanceTab = _uiState.value.distanceTab
            val currentState = _uiState.value.distancePagingStates[currentDistanceTab]

            try {
                if (!isInitial) {
                    val existingCards =
                        (currentState as? FeedPagingState.Success)?.feedCards ?: emptyList()
                    _uiState.update { state ->
                        val newStates = state.distancePagingStates.toMutableMap()
                        newStates[currentDistanceTab] =
                            FeedPagingState.LoadingMore(existingData = existingCards)
                        state.copy(distancePagingStates = newStates)
                    }
                } else {
                    _uiState.update { state ->
                        val newStates = state.distancePagingStates.toMutableMap()
                        newStates[currentDistanceTab] = FeedPagingState.Loading
                        state.copy(distancePagingStates = newStates)
                    }
                }

                val location = getLocationSafely()
                val lastId = if (isInitial) null else {
                    (currentState as? FeedPagingState.Success)?.lastId
                }

                when (val result = cardFeedRepository.requestFeedDistance(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    distance = currentDistanceTab.value,
                    lastId = lastId
                )) {
                    is DataResult.Fail -> {
                        _uiState.update { state ->
                            val newStates = state.distancePagingStates.toMutableMap()
                            newStates[currentDistanceTab] =
                                FeedPagingState.Error(message = result.message ?: "거리 피드 로딩 실패")
                            state.copy(distancePagingStates = newStates, refresh = false)//요기 수정
                        }
                    }

                    is DataResult.Success -> {
                        val newFeedCard = mapDistanceToFeedCard(result.data)
                        val existingCards = if (isInitial) emptyList() else {
                            (currentState as? FeedPagingState.Success)?.feedCards ?: emptyList()
                        }
                        _uiState.update { state ->
                            val newStates = state.distancePagingStates.toMutableMap()
                            newStates[currentDistanceTab] = FeedPagingState.Success(
                                feedCards = existingCards + newFeedCard,
                                hasNextPage = result.data.isNotEmpty(),
                                lastId = result.data.lastOrNull()?.cardId?.toLongOrNull()
                            )
                            state.copy(
                                location = location,
                                distancePagingStates = newStates,
                                refresh = false //요기 수정
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    val newStates = state.distancePagingStates.toMutableMap()
                    newStates[currentDistanceTab] =
                        FeedPagingState.Error(message = e.message ?: "거리 피드 로딩 실패")
                    state.copy(distancePagingStates = newStates)
                }
            }
        }
    }

    fun switchTab(feedType: FeedType) {
        _uiState.update { it.copy(currentTab = feedType, refresh = false) }
    }

    fun switchDistanceTab(distanceTab: DistanceType) {
        _uiState.update { state -> state.copy(distanceTab = distanceTab, refresh = false) }
        val cachedState = _uiState.value.distancePagingStates[distanceTab]
        if (cachedState == null || cachedState is FeedPagingState.Error) {
            loadDistanceFeeds(isInitial = true)
        }
    }

    fun loadMoreFeeds() {
        when (_uiState.value.currentTab) {
            FeedType.Latest -> loadLatestFeeds(isInitial = false)
            FeedType.Popular -> loadPopularFeeds(isInitial = false)
            FeedType.Distance -> loadDistanceFeeds(isInitial = false)
        }
    }

    fun addItemAsRead(notifyId: Long) {
        viewModelScope.launch {
            mutex.withLock {
                unreadIdBuffer.add(notifyId)
            }
        }
    }

    suspend fun readActivateNotify() {
        withContext(Dispatchers.IO) {
            while (isActive) {
                delay(2000L)
                val notify = mutex.withLock {
                    if (unreadIdBuffer.isEmpty()) return@withLock emptyList()
                    val ids = unreadIdBuffer.toList()
                    unreadIdBuffer.clear()
                    ids
                }
                if (notify.isNotEmpty()) {
                    when (val result =
                        readNotify.invoke(Param(notifyId = notify))) {
                        is DomainResult.Failure -> {
                            _uiState.update { state ->
                                state.copy(setReadNotify = UiState.Fail(result.error))
                            }
                        }

                        is DomainResult.Success -> {
                            _uiState.update { state ->
                                state.copy(setReadNotify = UiState.Success(Unit))
                            }
                        }
                    }
                }
            }
        }
    }

    // 요기 수정 -> 새로운 함수 생성 기존 refreshCurrentTab 명칭 사용
    fun refreshCurrentTab() {
        _uiState.update { state -> state.copy(refresh = true) }
        currentTab()
    }

    // 요기 수정 -> refreshCurrentTab -> currentTab으로 명칭 변경
    private fun currentTab() {
        when (_uiState.value.currentTab) {
            FeedType.Latest -> {
                // Latest는 Paging3를 사용하므로 수동 Loading 상태나 loadLatestFeeds 호출을 하지 않음
                triggerLatestFeedRefresh()
            }

            FeedType.Popular -> {
                // 이미 데이터가 있고 refresh 중이라면 Loading(초기화) 상태로 만들지 않음
                if (!_uiState.value.refresh) {
                    _uiState.update { it.copy(popularPagingState = FeedPagingState.Loading) }
                }
                loadPopularFeeds(isInitial = true)
            }

            FeedType.Distance -> {
                val currentDistanceTab = _uiState.value.distanceTab
                // 이미 데이터가 있고 refresh 중이라면 Loading(초기화) 상태로 만들지 않음
                if (!_uiState.value.refresh) {
                    _uiState.update { state ->
                        val newStates = state.distancePagingStates.toMutableMap()
                        newStates[currentDistanceTab] = FeedPagingState.Loading
                        state.copy(distancePagingStates = newStates)
                    }
                }
                loadDistanceFeeds(isInitial = true)
            }
        }
        refreshFeedNotice()
    }

    fun refreshFeedNotice() {
        _uiState.update { state -> state.copy(feedNotification = UiState.Loading) }
        getFeedNotice()
    }

    // TODO 개선 작업 필요 포인트
    private fun classifyLatestFeedType(item: Latest): FeedCardType {
        return when {
            !item.storyExpirationTime.isNullOrEmpty() -> FeedCardType.BoombType(
                cardId = item.cardId,
                storyExpirationTime = item.storyExpirationTime,
                content = item.cardContent,
                imageUrl = item.cardImgUrl,
                likeValue = item.likeCount.toString(),
                imageName = item.cardImageName,
                font = item.font,
                location = item.distance,
                writeTime = item.createAt,
                commentValue = item.commentCardCount.toString()
            )

            item.isAdminCard -> FeedCardType.AdminType(
                cardId = item.cardId,
                content = item.cardContent,
                imageUrl = item.cardImgUrl,
                imageName = item.cardImageName,
                font = item.font,
                location = item.distance,
                writeTime = item.createAt,
                commentValue = item.commentCardCount.toString(),
                likeValue = item.likeCount.toString()
            )

            else -> FeedCardType.NormalType(
                cardId = item.cardId,
                content = item.cardContent,
                imageUrl = item.cardImgUrl,
                imageName = item.cardImageName,
                font = item.font,
                location = item.distance,
                writeTime = item.createAt,
                commentValue = item.commentCardCount.toString(),
                likeValue = item.likeCount.toString()
            )
        }
    }

    private fun classifyDistanceFeedType(item: DistanceCard): FeedCardType {
        return when {
            !item.storyExpirationTime.isNullOrEmpty() -> FeedCardType.BoombType(
                cardId = item.cardId,
                storyExpirationTime = item.storyExpirationTime,
                content = item.cardContent,
                imageUrl = item.cardImgUrl,
                likeValue = item.likeCount.toString(),
                imageName = item.cardImageName,
                font = item.font,
                location = item.distance,
                writeTime = item.createAt,
                commentValue = item.commentCardCount.toString()
            )

            item.isAdminCard -> FeedCardType.AdminType(
                cardId = item.cardId,
                content = item.cardContent,
                imageUrl = item.cardImgUrl,
                imageName = item.cardImageName,
                font = item.font,
                location = item.distance,
                writeTime = item.createAt,
                commentValue = item.commentCardCount.toString(),
                likeValue = item.likeCount.toString()
            )

            else -> FeedCardType.NormalType(
                cardId = item.cardId,
                content = item.cardContent,
                imageUrl = item.cardImgUrl,
                imageName = item.cardImageName,
                font = item.font,
                location = item.distance,
                writeTime = item.createAt,
                commentValue = item.commentCardCount.toString(),
                likeValue = item.likeCount.toString()
            )
        }
    }

    private fun classifyPopularFeedType(item: Popular): FeedCardType {
        return when {
            !item.storyExpirationTime.isNullOrEmpty() -> FeedCardType.BoombType(
                cardId = item.cardId,
                storyExpirationTime = item.storyExpirationTime,
                content = item.cardContent,
                imageUrl = item.cardImgUrl,
                imageName = item.cardImageName,
                font = item.font,
                location = item.distance,
                writeTime = item.createAt,
                commentValue = item.commentCardCount.toString(),
                likeValue = item.likeCount.toString()
            )

            item.isAdminCard -> FeedCardType.AdminType(
                cardId = item.cardId,
                content = item.cardContent,
                imageUrl = item.cardImgUrl,
                imageName = item.cardImageName,
                font = item.font,
                location = item.distance,
                writeTime = item.createAt,
                commentValue = item.commentCardCount.toString(),
                likeValue = item.likeCount.toString()
            )

            else -> FeedCardType.NormalType(
                cardId = item.cardId,
                content = item.cardContent,
                imageUrl = item.cardImgUrl,
                imageName = item.cardImageName,
                font = item.font,
                location = item.distance,
                writeTime = item.createAt,
                commentValue = item.commentCardCount.toString(),
                likeValue = item.likeCount.toString()
            )
        }
    }

    private fun mapLatestToFeedCards(items: List<Latest>): List<FeedCardType> {
        return items.map { classifyLatestFeedType(it) }
    }

    private fun mapPopularToFeedCards(items: List<Popular>): List<FeedCardType> {
        return items.map { classifyPopularFeedType(it) }
    }

    private fun mapDistanceToFeedCard(items: List<DistanceCard>): List<FeedCardType> {
        return items.map { data -> classifyDistanceFeedType(data) }
    }

    fun removeFeedCard(cardId: String) {
        _uiState.update { currentState ->
            val updatedLatestState =
                if (currentState.latestPagingState is FeedPagingState.Success) {
                    currentState.latestPagingState.copy(
                        feedCards = currentState.latestPagingState.feedCards.filter { feedCard ->
                            when (feedCard) {
                                is FeedCardType.BoombType -> feedCard.cardId != cardId
                                is FeedCardType.AdminType -> feedCard.cardId != cardId
                                is FeedCardType.NormalType -> feedCard.cardId != cardId
                            }
                        }
                    )
                } else currentState.latestPagingState

            val updatedPopularState =
                if (currentState.popularPagingState is FeedPagingState.Success) {
                    currentState.popularPagingState.copy(
                        feedCards = currentState.popularPagingState.feedCards.filter { feedCard ->
                            when (feedCard) {
                                is FeedCardType.BoombType -> feedCard.cardId != cardId
                                is FeedCardType.AdminType -> feedCard.cardId != cardId
                                is FeedCardType.NormalType -> feedCard.cardId != cardId
                            }
                        }
                    )
                } else currentState.popularPagingState

            currentState.copy(
                latestPagingState = updatedLatestState,
                popularPagingState = updatedPopularState
            )
        }
    }

    fun navigateToDetail(cardId: String, isEventCard: Boolean) {
        if (_uiState.value.checkCardDelete is UiState.Loading) return
        val cardIdLong = cardId.toLongOrNull()
        if (cardIdLong == null) {
            _uiState.update { state ->
                state.copy(
                    checkCardDelete = UiState.Fail(ERROR_FAIL_JOB)
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { state -> state.copy(checkCardDelete = UiState.Loading) }
            when (val result = checkCardDelete(CheckCardAlreadyDelete.Param(cardId = cardIdLong))) {
                is DomainResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(checkCardDelete = UiState.Fail(result.error))
                    }
                }

                is DomainResult.Success -> {
                    if (result.data) {
                        _uiState.update { state -> state.copy(checkCardDelete = UiState.Success(cardIdLong)) }
                    } else {
                        _uiState.update { state -> state.copy(checkCardDelete = UiState.None) }
                        if (isEventCard) eventLog.moveToCardDetailWhenEventCard() else eventLog.moveToCardDetail()
                        _navigationEvent.emit(
                            NavigationEvent.NavigateToDetail(
                                CardDetailArgs(cardId = cardIdLong, previousView = CardDetailTrace.PROFILE)
                            )
                        )
                    }
                }
            }
        }
    }

    fun initCheckCardDelete() {
        val deletedCardId = when (val checkCardDelete = _uiState.value.checkCardDelete) {
            is UiState.Success<*> -> checkCardDelete.data as? Long
            else -> null
        }
        
        _uiState.update { state ->
            state.copy(checkCardDelete = UiState.None)
        }
        
        // 삭제된 카드 ID가 있다면 해당 카드를 숨김 목록에 추가
        deletedCardId?.let { cardId ->
            addToHiddenCards(cardId)
            // Popular/Distance 탭의 경우 추가로 리스트에서도 제거
            if (_uiState.value.currentTab != FeedType.Latest) {
                removeCardFromCurrentTab(cardId)
            }
        }
    }
    
    private fun addToHiddenCards(cardId: Long) {
        _uiState.update { state ->
            state.copy(
                hiddenCardIds = state.hiddenCardIds + cardId
            )
        }
    }
    
    private fun removeCardFromCurrentTab(cardId: Long) {
        when (_uiState.value.currentTab) {
            FeedType.Latest -> removeCardFromLatestTab(cardId)
            FeedType.Popular -> removeCardFromPopularTab(cardId)
            FeedType.Distance -> removeCardFromDistanceTab(cardId)
        }
    }
    
    private fun removeCardFromLatestTab(cardId: Long) {
        // Latest 탭은 UI 레벨에서 hiddenCardIds로 숨김 처리
        // 별도 처리 불필요
    }
    
    private fun removeCardFromPopularTab(cardId: Long) {
        val currentState = _uiState.value.popularPagingState
        if (currentState is FeedPagingState.Success) {
            val filteredCards = currentState.feedCards.filterNot { 
                when (it) {
                    is FeedCardType.BoombType -> it.cardId.toLongOrNull() == cardId
                    is FeedCardType.AdminType -> it.cardId.toLongOrNull() == cardId
                    is FeedCardType.NormalType -> it.cardId.toLongOrNull() == cardId
                }
            }
            _uiState.update { state ->
                state.copy(
                    popularPagingState = currentState.copy(feedCards = filteredCards)
                )
            }
        }
    }
    
    private fun removeCardFromDistanceTab(cardId: Long) {
        val currentDistanceTab = _uiState.value.distanceTab
        val currentState = _uiState.value.distancePagingStates[currentDistanceTab]
        if (currentState is FeedPagingState.Success) {
            val filteredCards = currentState.feedCards.filterNot { 
                when (it) {
                    is FeedCardType.BoombType -> it.cardId.toLongOrNull() == cardId
                    is FeedCardType.AdminType -> it.cardId.toLongOrNull() == cardId
                    is FeedCardType.NormalType -> it.cardId.toLongOrNull() == cardId
                }
            }
            val newStates = _uiState.value.distancePagingStates.toMutableMap()
            newStates[currentDistanceTab] = currentState.copy(feedCards = filteredCards)
            _uiState.update { state ->
                state.copy(
                    distancePagingStates = newStates
                )
            }
        }
    }

    fun setLoadNoticeView(data: Boolean) {
        _uiState.update { state ->
            state.copy(loadNoticeView = data)
        }
    }
}

sealed interface NavigationEvent {
    data class NavigateToDetail(val args: CardDetailArgs) : NavigationEvent
}

data class Home(
    val currentTab: FeedType = FeedType.Latest,
    val distanceTab: DistanceType = DistanceType.KM_1,
    val latestPagingState: FeedPagingState = FeedPagingState.None,
    val popularPagingState: FeedPagingState = FeedPagingState.None,
    val distancePagingStates: Map<DistanceType, FeedPagingState> = emptyMap(),
    val refresh: Boolean = false, //요기 수정 변수 사용
    val feedItem: List<FeedData> = emptyList(),
    val notifyItem: List<Notify> = emptyList(),
    val location: Location = Location.EMPTY,
    val shouldShowPermissionRationale: Boolean = false,
    val feedNotification: UiState<List<Notice>> = UiState.Loading,
    val setReadNotify: UiState<Unit> = UiState.Loading,
    val checkCardDelete: UiState<Long> = UiState.None,
    val hiddenCardIds: Set<Long> = emptySet(),
    val loadNoticeView : Boolean = false
) {
    val currentPagingState: FeedPagingState
        get() = when (currentTab) {
            FeedType.Latest -> latestPagingState
            FeedType.Popular -> popularPagingState
            FeedType.Distance -> distancePagingStates[distanceTab] ?: FeedPagingState.None
        }
}

enum class FeedType {
    Latest, Popular, Distance
}

enum class DistanceType(val value: Double) {
    KM_1(1.0),
    KM_5(5.0),
    KM_10(10.0),
    KM_20(20.0),
    KM_50(50.0);
}

sealed interface UiState<out T> {
    data object None : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Fail(val errorMessage: String) : UiState<Nothing>
}

sealed interface FeedPagingState {
    data object None : FeedPagingState
    data object Loading : FeedPagingState
    data class LoadingMore(val existingData: List<FeedCardType>) : FeedPagingState
    data class Success(
        val feedCards: List<FeedCardType>,
        val hasNextPage: Boolean,
        val lastId: Long?,
    ) : FeedPagingState

    data class Error(val message: String) : FeedPagingState
}

private const val TAG = "FeedViewModel"
