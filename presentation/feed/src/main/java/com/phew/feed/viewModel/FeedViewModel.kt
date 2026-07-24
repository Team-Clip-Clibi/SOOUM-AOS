package com.phew.feed.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.phew.core.ui.model.navigation.CardDetailArgs
import com.phew.core_common.CardDetailTrace
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_ALREADY_CARD_DELETE
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_NO_DATA
import com.phew.domain.dto.CardArticle
import com.phew.domain.dto.FeedCardType
import com.phew.domain.dto.Location
import com.phew.domain.dto.Notice
import com.phew.domain.dto.NoticeSource
import com.phew.domain.dto.Notification
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.FeedPagingFactory
import com.phew.domain.repository.FeedPagingQuery
import com.phew.domain.usecase.CheckCardAlreadyDelete
import com.phew.domain.usecase.CheckLocationPermission
import com.phew.domain.usecase.GetCardArticle
import com.phew.domain.usecase.GetFeedNotification
import com.phew.domain.usecase.GetNotification
import com.phew.domain.usecase.GetReadNotification
import com.phew.domain.usecase.GetUnReadNotification
import com.phew.domain.usecase.LikeCard
import com.phew.domain.usecase.RunHaptic
import com.phew.domain.usecase.SaveEventLogFeedView
import com.phew.domain.usecase.SetReadActivateNotify
import com.phew.domain.usecase.SetReadActivateNotify.*
import com.phew.domain.usecase.UnlikeCard
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val locationAsk: CheckLocationPermission,
    getNotificationPage: GetNotification,
    getUnReadNotification: GetUnReadNotification,
    getReadNotification: GetReadNotification,
    private val feedPagingFactory: FeedPagingFactory,
    private val deviceRepository: DeviceRepository,
    private val notification: GetFeedNotification,
    private val readNotify: SetReadActivateNotify,
    private val checkCardDelete: CheckCardAlreadyDelete,
    private val eventLog: SaveEventLogFeedView,
    private val getArticle: GetCardArticle,
    private val likeCard: LikeCard,
    private val unlikeCard: UnlikeCard,
    private val runHaptic: RunHaptic,
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
        viewModelScope.launch {
            launch { loadInitialFeeds() }
            launch { getFeedNotice() }
            launch { fetchCardArticle() }
        }
    }

    private suspend fun loadInitialFeeds() {
        // Location 초기 설정
        val location = getLocationSafely()
        _uiState.update { state ->
            state.copy(location = location)
        }
        _feedPagingSelection.update { selection ->
            selection.copy(
                latitude = location.latitude.takeIf { it != 0.0 },
                longitude = location.longitude.takeIf { it != 0.0 }
            )
        }
    }

    val notice: Flow<PagingData<Notice>> =
        getNotificationPage(NoticeSource.NOTIFICATION).cachedIn(viewModelScope)
    val unReadActivateAlarm: Flow<PagingData<Notification>> =
        getUnReadNotification().cachedIn(viewModelScope)
    val readActivateAlarm: Flow<PagingData<Notification>> =
        getReadNotification().cachedIn(viewModelScope)

    private data class FeedPagingSelection(
        val feedType: FeedType,
        val distanceType: DistanceType,
        val latitude: Double?,
        val longitude: Double?,
        val refreshToken: Long = 0L,
    ) {
        fun toQuery(): FeedPagingQuery = when (feedType) {
            FeedType.Latest -> FeedPagingQuery.Latest(
                latitude = latitude,
                longitude = longitude
            )

            FeedType.Popular -> FeedPagingQuery.Popular(
                latitude = latitude,
                longitude = longitude
            )

            FeedType.Distance -> FeedPagingQuery.Distance(
                latitude = latitude,
                longitude = longitude,
                distance = distanceType.value
            )
        }
    }

    private val _feedPagingSelection = MutableStateFlow(
        FeedPagingSelection(
            feedType = FeedType.Latest,
            distanceType = DistanceType.KM_1,
            latitude = null,
            longitude = null
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val feedPaging: Flow<PagingData<FeedCardType>> = _feedPagingSelection
        .flatMapLatest { selection -> feedPagingFactory.create(selection.toQuery()) }
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
            updateFeedSelection()
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

    fun switchTab(feedType: FeedType) {
        _uiState.update { it.copy(currentTab = feedType) }
        updateFeedSelection()
    }

    fun switchDistanceTab(distanceTab: DistanceType) {
        _uiState.update { state -> state.copy(distanceTab = distanceTab) }
        updateFeedSelection()
    }

    private fun updateFeedSelection(refresh: Boolean = false) {
        val state = _uiState.value
        _feedPagingSelection.update { selection ->
            selection.copy(
                feedType = state.currentTab,
                distanceType = state.distanceTab,
                latitude = state.location.latitude.takeIf { it != 0.0 },
                longitude = state.location.longitude.takeIf { it != 0.0 },
                refreshToken = if (refresh) selection.refreshToken + 1 else selection.refreshToken
            )
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

    fun refreshCurrentTab() {
        fetchCardArticle()
        _uiState.update { state ->
            state.copy(
                feedLikeStates = state.feedLikeStates.mapValues { (_, likeState) ->
                    likeState.copy(
                        isLoading = false,
                        animationVersion = 0,
                    )
                }
            )
        }
        updateFeedSelection(refresh = true)
    }

    fun onFeedRefreshCompleted() {
        _uiState.update { state -> state.copy(feedLikeStates = emptyMap()) }
    }

    fun verifyAndToggleLike(
        cardId: Long,
        initialLikeCount: Int,
        initialIsLike: Boolean,
    ) {
        val currentState = _uiState.value.feedLikeStates[cardId]
        if (currentState?.isLoading == true) return
        runHaptic()

        val stateBeforeToggle = currentState ?: FeedLikeUiState(
            isLike = initialIsLike,
            likeCount = initialLikeCount,
        )
        val toggledIsLike = !stateBeforeToggle.isLike
        val toggledLikeCount = if (toggledIsLike) {
            stateBeforeToggle.likeCount + 1
        } else {
            (stateBeforeToggle.likeCount - 1).coerceAtLeast(0)
        }
        updateFeedLikeState(
            cardId = cardId,
            state = stateBeforeToggle.copy(
                isLike = toggledIsLike,
                likeCount = toggledLikeCount,
                isLoading = true,
                animationVersion = stateBeforeToggle.animationVersion + 1,
            ),
        )
        viewModelScope.launch {
            when (val checkResult = checkCardDelete(CheckCardAlreadyDelete.Param(cardId))) {
                is DomainResult.Success -> {
                    if (checkResult.data) {
                        updateFeedLikeState(cardId, stateBeforeToggle.copy(isLoading = false))
                        _uiState.update { state ->
                            state.copy(checkCardDelete = UiState.Success(cardId))
                        }
                        return@launch
                    }
                }

                is DomainResult.Failure -> {
                    updateFeedLikeState(cardId, stateBeforeToggle.copy(isLoading = false))
                    _uiState.update { state ->
                        state.copy(checkCardDelete = UiState.Fail(checkResult.error))
                    }
                    return@launch
                }
            }

            val result = if (stateBeforeToggle.isLike) {
                unlikeCard(cardId)
            } else {
                likeCard(cardId)
            }

            when (result) {
                is DomainResult.Success -> {
                    updateFeedLikeState(
                        cardId = cardId,
                        state = stateBeforeToggle.copy(
                            isLike = toggledIsLike,
                            likeCount = toggledLikeCount,
                            isLoading = false,
                            animationVersion = stateBeforeToggle.animationVersion + 1,
                        )
                    )
                }

                is DomainResult.Failure -> {
                    updateFeedLikeState(cardId, stateBeforeToggle.copy(isLoading = false))
                    if (result.error == ERROR_ALREADY_CARD_DELETE) {
                        _uiState.update { state ->
                            state.copy(checkCardDelete = UiState.Success(cardId))
                        }
                    }
                }
            }
        }
    }

    private fun updateFeedLikeLoading(cardId: Long, isLoading: Boolean) {
        _uiState.update { currentState ->
            val likeState = currentState.feedLikeStates[cardId] ?: return@update currentState
            currentState.copy(
                feedLikeStates = currentState.feedLikeStates +
                    (cardId to likeState.copy(isLoading = isLoading))
            )
        }
    }

    private fun updateFeedLikeState(cardId: Long, state: FeedLikeUiState) {
        _uiState.update { currentState ->
            currentState.copy(
                feedLikeStates = currentState.feedLikeStates + (cardId to state)
            )
        }
    }

    fun removeFeedCard(cardId: String) {
        cardId.toLongOrNull()?.let(::addToHiddenCards)
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
        }
    }

    private fun fetchCardArticle() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = getArticle()) {
                is DomainResult.Failure -> {
                    if (result.error == ERROR_NO_DATA) {
                        _uiState.update { state ->
                            state.copy(cardArticle = UiState.None)
                        }
                        return@launch
                    }
                    _uiState.update { state ->
                        state.copy(cardArticle = UiState.Fail(result.error))
                    }
                }

                is DomainResult.Success -> {
                    _uiState.update { state ->
                        state.copy(cardArticle = UiState.Success(result.data))
                    }
                }
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

    fun deleteNotice(noticeId: Int) {
        _uiState.update { currentState ->
            if (currentState.feedNotification is UiState.Success) {
                val currentNotices = currentState.feedNotification.data
                val updatedNotices = currentNotices.filter { it.id != noticeId }
                currentState.copy(
                    feedNotification = UiState.Success(updatedNotices)
                )
            } else {
                currentState
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
    val location: Location = Location.EMPTY,
    val shouldShowPermissionRationale: Boolean = false,
    val feedNotification: UiState<List<Notice>> = UiState.Loading,
    val setReadNotify: UiState<Unit> = UiState.Loading,
    val checkCardDelete: UiState<Long> = UiState.None,
    val hiddenCardIds: Set<Long> = emptySet(),
    val cardArticle: UiState<List<CardArticle>> = UiState.Loading,
    val feedLikeStates: Map<Long, FeedLikeUiState> = emptyMap(),
)

data class FeedLikeUiState(
    val isLike: Boolean,
    val likeCount: Int,
    val isLoading: Boolean = false,
    val animationVersion: Int = 0,
)

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
