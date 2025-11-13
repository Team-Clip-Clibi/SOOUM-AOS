package com.phew.feed.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.phew.core.ui.model.navigation.CardDetailArgs
import com.phew.domain.dto.FeedData
import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.domain.dto.DistanceCard
import com.phew.domain.dto.FeedCardType
import com.phew.domain.dto.Latest
import com.phew.domain.dto.Location
import com.phew.domain.dto.Notice
import com.phew.domain.dto.Notification
import com.phew.domain.dto.Notify
import com.phew.domain.dto.Popular
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.network.CardFeedRepository
import com.phew.domain.usecase.CheckLocationPermission
import com.phew.domain.usecase.CreateImageFile
import com.phew.domain.usecase.FinishTakePicture
import com.phew.domain.usecase.GetFeedNotification
import com.phew.domain.usecase.GetNotification
import com.phew.domain.usecase.GetReadNotification
import com.phew.domain.usecase.GetUnReadNotification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val locationAsk: CheckLocationPermission,
    getNotificationPage: GetNotification,
    getUnReadNotification: GetUnReadNotification,
    getReadNotification: GetReadNotification,
    private val cardFeedRepository: CardFeedRepository,
    private val deviceRepository: DeviceRepository,
    private val notification: GetFeedNotification,
) :
    ViewModel() {
    private val _uiState = MutableStateFlow(Home())
    val uiState: StateFlow<Home> = _uiState.asStateFlow()

    // Navigation side effects
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    /**
     * 공지사항(notice)
     * 활동알림(unRead , read)
     */

    init {
        // 홈 화면 진입 시 최신 피드 자동 로딩
        loadInitialFeeds()
        getFeedNotice()
        // 탭 변경 감지하여 필요시 데이터 로딩
        // TODO 개선 포인트 !! 수정해라 JG
        viewModelScope.launch {
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
    }

    private fun loadInitialFeeds() {
        viewModelScope.launch {
            _uiState.update { it.copy(latestPagingState = FeedPagingState.Loading) }
            loadLatestFeeds(isInitial = true)
        }
    }

    val notice: Flow<PagingData<Notice>> = getNotificationPage().cachedIn(viewModelScope)
    val unReadNotification: Flow<PagingData<Notification>> =
        getUnReadNotification().cachedIn(viewModelScope)
    val readNotification: Flow<PagingData<Notification>> =
        getReadNotification().cachedIn(viewModelScope)

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
        }
    }

    private fun getFeedNotice() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val request = notification()) {
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
            try {
                val currentState = _uiState.value.latestPagingState

                if (!isInitial) {
                    val existingCards =
                        (currentState as? FeedPagingState.Success)?.feedCards ?: emptyList()
                    _uiState.update {
                        it.copy(latestPagingState = FeedPagingState.LoadingMore(existingCards))
                    }
                }

                val location = getLocationSafely()
                val latitude = location.latitude.takeIf { it != 0.0 }
                val longitude = location.longitude.takeIf { it != 0.0 }

                val lastId = if (isInitial) null else {
                    (currentState as? FeedPagingState.Success)?.lastId
                }

                when (val result = cardFeedRepository.requestFeedLatest(
                    latitude = latitude,
                    longitude = longitude,
                    lastId = lastId
                )) {
                    is DataResult.Success -> {
                        // TODO 여기 수정 필요해보임 (신규 카드 요청하는데 기존 카드와 동일한 데이터를 요청해서 받아오는 원인 같아보임)
                        val newFeedCards = mapLatestToFeedCards(result.data)
                        val existingCards = if (isInitial) emptyList() else {
                            (currentState as? FeedPagingState.Success)?.feedCards ?: emptyList()
                        }

                        _uiState.update { state ->
                            state.copy(
                                location = location,
                                latestPagingState = FeedPagingState.Success(
                                    feedCards = existingCards + newFeedCards,
                                    hasNextPage = result.data.isNotEmpty(),
                                    lastId = result.data.lastOrNull()?.cardId?.toIntOrNull()
                                )
                            )
                        }
                    }

                    is DataResult.Fail -> {
                        _uiState.update {
                            it.copy(
                                latestPagingState = FeedPagingState.Error(
                                    result.message ?: "최신 피드 로딩 실패"
                                )
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

    // TODO 개선 작업 필요 포인트
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
                                    hasNextPage = result.data.isNotEmpty(),
                                    lastId = null // Popular는 페이징이 없으므로 null
                                )
                            )
                        }
                    }

                    is DataResult.Fail -> {
                        _uiState.update {
                            it.copy(
                                popularPagingState = FeedPagingState.Error(
                                    result.message ?: "인기 피드 로딩 실패"
                                )
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
                            state.copy(distancePagingStates = newStates)
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
                                lastId = result.data.lastOrNull()?.cardId?.toIntOrNull()
                            )
                            state.copy(
                                location = location,
                                distancePagingStates = newStates
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
        _uiState.update { it.copy(currentTab = feedType) }
    }

    fun switchDistanceTab(distanceTab: DistanceType) {
        _uiState.update { state -> state.copy(distanceTab = distanceTab) }
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

    fun refreshCurrentTab() {
        when (_uiState.value.currentTab) {
            FeedType.Latest -> {
                _uiState.update { it.copy(latestPagingState = FeedPagingState.Loading) }
                loadLatestFeeds(isInitial = true)
            }

            FeedType.Popular -> {
                _uiState.update { it.copy(popularPagingState = FeedPagingState.Loading) }
                loadPopularFeeds(isInitial = true)
            }

            FeedType.Distance -> {
                val currentDistanceTab = _uiState.value.distanceTab
                _uiState.update { state ->
                    val newStates = state.distancePagingStates.toMutableMap()
                    newStates[currentDistanceTab] = FeedPagingState.Loading
                    state.copy(distancePagingStates = newStates)
                }
                loadDistanceFeeds(isInitial = true)
            }
        }
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

    // TODO 개선 작업 필요 포인트
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

    fun navigateToDetail(cardId: String) {
        viewModelScope.launch {
            val cardIdLong = cardId.toLongOrNull()
            if (cardIdLong != null) {
                _navigationEvent.emit(NavigationEvent.NavigateToDetail(CardDetailArgs(cardIdLong)))
            }
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
    val refresh: UiState<Boolean> = UiState.None,
    val feedItem: List<FeedData> = emptyList(),
    val notifyItem: List<Notify> = emptyList(),
    val location: Location = Location.EMPTY,
    val shouldShowPermissionRationale: Boolean = false,
    val feedNotification: UiState<List<Notice>> = UiState.Loading,
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
    KM_15(15.0),
    KM_20(20.0),
    KM_50(50.0);

    companion object {
        fun fromValue(value: Double): DistanceType {
            return entries.find { distanceType -> distanceType.value == value } ?: KM_1
        }
    }
}

sealed interface UiState<out T> {
    data object None : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Fail(val errorMessage: String) : UiState<Nothing>
}

// TODO 개선 작업 필요 포인트  State Interface가 많음. 정리 필요해 보임.
sealed interface FeedPagingState {
    data object None : FeedPagingState
    data object Loading : FeedPagingState
    data class LoadingMore(val existingData: List<FeedCardType>) : FeedPagingState
    data class Success(
        val feedCards: List<FeedCardType>,
        val hasNextPage: Boolean,
        val lastId: Int?,
    ) : FeedPagingState

    data class Error(val message: String) : FeedPagingState
}