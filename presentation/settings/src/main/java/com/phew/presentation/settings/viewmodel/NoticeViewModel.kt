package com.phew.presentation.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.phew.core_common.DomainResult
import com.phew.domain.dto.Notice
import com.phew.domain.usecase.GetFeedNotification
import com.phew.domain.usecase.GetNotification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val notification: GetFeedNotification,
    getNotificationPage: GetNotification,
): ViewModel(){

    private val _uiState = MutableStateFlow(NoticeState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NoticeNavigationEvent>()
    val navigationEvent: SharedFlow<NoticeNavigationEvent> = _navigationEvent.asSharedFlow()

    val notice: Flow<PagingData<Notice>> = getNotificationPage().cachedIn(viewModelScope)

    init {
        getFeedNotice()
    }

    private fun getFeedNotice() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val request = notification()) {
                is DomainResult.Failure -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            noticeItem = UiState.Fail(request.error)
                        ) 
                    }
                }

                is DomainResult.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            noticeItem = UiState.Success(request.data)
                        ) 
                    }
                }
            }
        }
    }
    
    fun onNoticeItemClick(notice: Notice) {
        viewModelScope.launch {
            _navigationEvent.emit(NoticeNavigationEvent.NavigateToNoticeDetail(notice))
        }
    }
    
    fun refresh() {
        getFeedNotice()
    }
}

data class NoticeState(
    val isLoading: Boolean = false,
    val noticeItem: UiState<List<Notice>> = UiState.None
)

sealed class NoticeNavigationEvent {
    data class NavigateToNoticeDetail(val notice: Notice) : NoticeNavigationEvent()
}


sealed interface UiState<out T> {
    data object None : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Fail(val errorMessage: String) : UiState<Nothing>
}