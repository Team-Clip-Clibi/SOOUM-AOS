package com.phew.home.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.phew.domain.dto.FeedData
import com.phew.domain.dto.FeedCardType
import com.phew.domain.dto.Latest
import com.phew.domain.dto.Notice
import com.phew.domain.dto.Notification
import com.phew.domain.dto.Notify
import com.phew.domain.dto.Popular
import com.phew.domain.repository.network.CardFeedRepository
import com.phew.domain.usecase.CheckLocationPermission
import com.phew.domain.usecase.GetNotification
import com.phew.domain.usecase.GetReadNotification
import com.phew.domain.usecase.GetUnReadNotification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.update
import com.phew.core_common.DataResult
import com.phew.domain.dto.Location
import com.phew.domain.repository.DeviceRepository


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val locationAsk: CheckLocationPermission,
    getNotificationPage: GetNotification,
    getUnReadNotification: GetUnReadNotification,
    getReadNotification: GetReadNotification,
    private val cardFeedRepository: CardFeedRepository,
    private val deviceRepository: DeviceRepository
) :
    ViewModel() {
    private val _uiState = MutableStateFlow(Home())
    val uiState: StateFlow<Home> = _uiState.asStateFlow()
    /**
     * 공지사항(notice)
     * 활동알림(unRead , read)
     */

    init {
        // 홈 화면 진입 시 최신 피드 자동 로딩
        loadLatestFeeds()
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
            //TODO 근처 피드 게시물 가져오기
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
            Location.EMPTY
        }
    }

    private fun getLocation() {
        viewModelScope.launch {
            val location = getLocationSafely()
            _uiState.update { state ->
                state.copy(location = location)
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

    private fun loadLatestFeeds() {
        if (_uiState.value.latestState is UiState.Loading) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(latestState = UiState.Loading) }

            try {
                // 위치 정보 가져오기 완료 대기 (오류 무시)
                val location = getLocationSafely()
                _uiState.update { it.copy(location = location) }

                // 위치 정보 유무와 관계없이 피드 로딩 진행
                val latitude = location.latitude.takeIf { it != 0.0 }
                val longitude = location.longitude.takeIf { it != 0.0 }

                when (val result = cardFeedRepository.requestFeedLatest(
                    latitude = latitude,
                    longitude = longitude,
                    lastId = null
                )) {
                    is DataResult.Success -> {
                        val feedCards = mapLatestToFeedCards(result.data)
                        _uiState.update { 
                            it.copy(
                                latestState = UiState.Success(result.data),
                                feedCards = feedCards
                            ) 
                        }
                    }
                    is DataResult.Fail -> {
                        _uiState.update {
                            it.copy(latestState = UiState.Fail(result.message ?: "최신 피드 로딩 실패"))
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(latestState = UiState.Fail(e.message ?: "최신 피드 로딩 실패"))
                }
            }
        }
    }

    private fun loadPopularFeeds() {
        if (_uiState.value.popularState is UiState.Loading) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(popularState = UiState.Loading) }

            try {
                // 위치 정보 가져오기 완료 대기 (오류 무시)
                val location = getLocationSafely()
                _uiState.update { it.copy(location = location) }

                // 위치 정보 유무와 관계없이 피드 로딩 진행
                val latitude = location.latitude.takeIf { it != 0.0 }
                val longitude = location.longitude.takeIf { it != 0.0 }

                when (val result = cardFeedRepository.requestFeedPopular(
                    latitude = latitude,
                    longitude = longitude
                )) {
                    is DataResult.Success -> {
                        val feedCards = mapPopularToFeedCards(result.data)
                        _uiState.update { 
                            it.copy(
                                popularState = UiState.Success(result.data),
                                feedCards = feedCards
                            ) 
                        }
                    }
                    is DataResult.Fail -> {
                        _uiState.update {
                            it.copy(popularState = UiState.Fail(result.message ?: "인기 피드 로딩 실패"))
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(popularState = UiState.Fail(e.message ?: "인기 피드 로딩 실패"))
                }
            }
        }
    }

    fun switchTab(feedType: FeedType) {
        _uiState.update { it.copy(currentTab = feedType) }

        when (feedType) {
            FeedType.Latest -> {
                if (_uiState.value.latestState is UiState.None) {
                    loadLatestFeeds()
                }
            }
            FeedType.Popular -> {
                if (_uiState.value.popularState is UiState.None) {
                    loadPopularFeeds()
                }
            }
        }
    }

    fun refreshCurrentTab() {
        when (_uiState.value.currentTab) {
            FeedType.Latest -> loadLatestFeeds()
            FeedType.Popular -> loadPopularFeeds()
        }
    }

    private fun classifyLatestFeedType(item: Latest): FeedCardType {
        return when {
            item.storyExpirationTime.isNotEmpty() -> FeedCardType.BoombType(
                cardId = item.cardId,
                storyExpirationTime = item.storyExpirationTime,
                content = item.cardContent,
                imageUrl = item.cardImgUrl,
                imageName = item.cardImagName,
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
                imageName = item.cardImagName,
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
                imageName = item.cardImagName,
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
            item.storyExpirationTime.isNotEmpty() -> FeedCardType.BoombType(
                cardId = item.cardId,
                storyExpirationTime = item.storyExpirationTime,
                content = item.cardContent,
                imageUrl = item.cardImgUrl,
                imageName = item.cardImagName,
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
                imageName = item.cardImagName,
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
                imageName = item.cardImagName,
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

    fun removeFeedCard(cardId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                feedCards = currentState.feedCards.filter { feedCard ->
                    when (feedCard) {
                        is FeedCardType.BoombType -> feedCard.cardId != cardId
                        is FeedCardType.AdminType -> feedCard.cardId != cardId
                        is FeedCardType.NormalType -> feedCard.cardId != cardId
                    }
                }
            )
        }
    }

}

data class Home(
    val currentTab: FeedType = FeedType.Latest,
    val latestState: UiState<List<Latest>> = UiState.None,
    val popularState: UiState<List<Popular>> = UiState.None,
    val refresh: UiState<Boolean> = UiState.None,
    val feedItem: List<FeedData> = emptyList(),
    val feedCards: List<FeedCardType> = emptyList(),
    val notifyItem: List<Notify> = emptyList(),
    val location: Location = Location.EMPTY,
    val shouldShowPermissionRationale: Boolean = false,
)

enum class FeedType {
    Latest, Popular
}

sealed interface UiState<out T> {
    data object None : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Fail(val errorMessage: String) : UiState<Nothing>
}