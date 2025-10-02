package com.phew.home.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.phew.domain.dto.FeedData
import com.phew.domain.dto.Notice
import com.phew.domain.dto.Notification
import com.phew.domain.dto.Notify
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


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val locationAsk: CheckLocationPermission,
    getNotificationPage: GetNotification,
    getUnReadNotification: GetUnReadNotification,
    getReadNotification: GetReadNotification,
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
    /**
     * 권한 요청
     */
    private val _requestPermissionEvent = MutableSharedFlow<Array<String>>()
    val requestPermissionEvent = _requestPermissionEvent.asSharedFlow()

    fun checkLocationPermission() {
        val isGranted = locationAsk()
        if (isGranted) {
            //TODO 근처 피드 게시물 가져오기
            return
        }
        _uiState.update { state ->
            state.copy(shouldShowPermissionRationale = true)
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

}

data class Home(
    val refresh: UiState<Boolean> = UiState.None,
    val feedItem: List<FeedData> = emptyList(),
    val notifyItem: List<Notify> = emptyList(),
    val shouldShowPermissionRationale: Boolean = false,
)

sealed interface UiState<out T> {
    data object None : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Fail(val errorMessage: String) : UiState<Nothing>
}