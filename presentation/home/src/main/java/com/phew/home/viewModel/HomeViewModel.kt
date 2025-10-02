package com.phew.home.viewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.phew.domain.dto.FeedData
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.update
import com.phew.core_common.DataResult


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val locationAsk: CheckLocationPermission,
    private val getNotificationPage: GetNotification,
    private val getUnReadNotification: GetUnReadNotification,
    private val getReadNotification: GetReadNotification,
    private val cardFeedRepository: CardFeedRepository
) :
    ViewModel() {
    private val _uiState = MutableStateFlow(Home())
    val uiState: StateFlow<Home> = _uiState.asStateFlow()

    /**
     * 공지사항(notice)
     * 활동알림(unRead , read)
     */
    val notice: Flow<PagingData<Notice>> = getNotificationPage().cachedIn(viewModelScope)
    val unReadNotification: Flow<PagingData<Notification>> =
        getUnReadNotification().cachedIn(viewModelScope)
    val readNotification: Flow<PagingData<Notification>> =
        getReadNotification().cachedIn(viewModelScope)

    init {
        loadLatestFeeds()
    }

    fun refresh() {
        if (_uiState.value.refresh is UiState.Loading) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(refresh = UiState.Loading)
            try {
                delay(5000)
                val newFeedItems = (1..10).map { it ->
                    FeedData(
                        location = "정자동",
                        writeTime = when (it) {
                            1 -> "1시간전"
                            2 -> "2025-09-21"
                            3 -> "2025-09-20"
                            else -> "2025-09-19"
                        },
                        commentValue = "1",
                        likeValue = "1",
                        uri = Uri.EMPTY,
                        content = "test$it"
                    )
                }
                _uiState.value = _uiState.value.copy(
                    feedItem = newFeedItems,
                    refresh = UiState.Success(true)
                )
                _uiState.value = _uiState.value.copy(
                    refresh = UiState.None
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    refresh = UiState.Fail(e.message ?: "새로고침 실패")
                )
            }
        }
    }

    fun checkLocationPermission() {
        viewModelScope.launch(Dispatchers.IO) {
            val isAsk = locationAsk()
            _uiState.update { it.copy(isLocationAsk = isAsk) }
        }
    }

    fun loadLatestFeeds() {
        if (_uiState.value.latestState is UiState.Loading) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(latestState = UiState.Loading) }
            
            try {
                val latitude = if (_uiState.value.isLocationAsk) null else null // TODO: 실제 위치 정보 가져오기
                val longitude = if (_uiState.value.isLocationAsk) null else null // TODO: 실제 위치 정보 가져오기
                
                when (val result = cardFeedRepository.requestFeedLatest(
                    latitude = latitude,
                    longitude = longitude,
                    lastId = null
                )) {
                    is DataResult.Success -> {
                        _uiState.update { it.copy(latestState = UiState.Success(result.data)) }
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

    fun loadPopularFeeds() {
        if (_uiState.value.popularState is UiState.Loading) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(popularState = UiState.Loading) }
            
            try {
                val latitude = if (_uiState.value.isLocationAsk) null else null // TODO: 실제 위치 정보 가져오기
                val longitude = if (_uiState.value.isLocationAsk) null else null // TODO: 실제 위치 정보 가져오기
                
                when (val result = cardFeedRepository.requestFeedPopular(
                    latitude = latitude,
                    longitude = longitude
                )) {
                    is DataResult.Success -> {
                        _uiState.update { it.copy(popularState = UiState.Success(result.data)) }
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


}

data class Home(
    val currentTab: FeedType = FeedType.Latest,
    val latestState: UiState<List<Latest>> = UiState.None,
    val popularState: UiState<List<Popular>> = UiState.None,
    val refresh: UiState<Boolean> = UiState.None,
    val feedItem: List<FeedData> = emptyList(),
    val notifyItem: List<Notify> = emptyList(),
    val isLocationAsk: Boolean = true
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