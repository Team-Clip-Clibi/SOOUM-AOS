package com.phew.presentation.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phew.core_common.DomainResult
import com.phew.domain.usecase.GetTransferCode
import com.phew.domain.usecase.RefreshTransferCode
import com.phew.presentation.settings.model.LoginOtherDeviceNavigationEvent
import com.phew.presentation.settings.model.LoginOtherDeviceUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.phew.domain.usecase.GetRefreshToken

@HiltViewModel
class LoginOtherDeviceViewModel @Inject constructor(
    private val getTransferCode: GetTransferCode,
    private val refreshTransferCode: RefreshTransferCode,
    private val getRefreshToken: GetRefreshToken
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginOtherDeviceUiState())
    val uiState: StateFlow<LoginOtherDeviceUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<LoginOtherDeviceNavigationEvent>()
    val navigationEvent: SharedFlow<LoginOtherDeviceNavigationEvent> = _navigationEvent.asSharedFlow()

    private var timerJob: Job? = null
    private var remainingTimeMillis = 0L

    companion object {
        private const val ONE_HOUR_MILLIS = 60 * 60 * 1000L // 1시간
        private const val TIMER_INTERVAL = 1000L // 1초
        private const val ERROR_CODE_SERVER = 500
    }

    init {
        generateCode()
    }

    private fun generateCode() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = getTransferCode()) {
                is DomainResult.Success -> {
                    _uiState.update {
                        it.copy(
                            code = result.data.transferCode,
                            expiredAt = result.data.expiredAt,
                            isCodeGenerated = true,
                            isLoading = false
                        )
                    }
                    startTimer()
                }
                is DomainResult.Failure -> {
                     val error = result.error
                     if (error == ERROR_CODE_SERVER) {
                         val refreshToken = getRefreshToken()
                         _uiState.update {
                             it.copy(
                                 isLoading = false,
                                 showErrorDialog = true,
                                 refreshToken = refreshToken
                             )
                         }
                     } else {
                         _uiState.update {
                             it.copy(
                                 isLoading = false,
                             )
                         }
                     }
                }
            }
        }
    }


    private fun startTimer() {
        timerJob?.cancel()
        remainingTimeMillis = ONE_HOUR_MILLIS

        timerJob = viewModelScope.launch {
            while (remainingTimeMillis > 0) {
                updateTimerText()
                delay(TIMER_INTERVAL)
                remainingTimeMillis -= TIMER_INTERVAL
            }
            // 타이머 종료
            _uiState.update { 
                it.copy(
                    remainingTimeText = "00:00"
                )
            }
        }
    }

    private fun updateTimerText() {
        val totalMinutes = remainingTimeMillis / (60 * 1000)
        val seconds = (remainingTimeMillis % (60 * 1000)) / 1000

        val timeText = String.format("%02d:%02d", totalMinutes, seconds)

        _uiState.update { it.copy(remainingTimeText = timeText) }
    }

    fun onBackPressed() {
        viewModelScope.launch {
            _navigationEvent.emit(LoginOtherDeviceNavigationEvent.NavigateBack)
        }
    }

    fun onRetryCodeClick() {
        refreshCodeFromApi()
    }
    
    private fun refreshCodeFromApi() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = refreshTransferCode()) {
                is DomainResult.Success -> {
                    _uiState.update {
                        it.copy(
                            code = result.data.transferCode,
                            expiredAt = result.data.expiredAt,
                            isCodeGenerated = true,
                            isLoading = false
                        )
                    }
                    startTimer()
                }
                is DomainResult.Failure -> {
                    val error = result.error
                    if (error == ERROR_CODE_SERVER) {
                        val refreshToken = getRefreshToken()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                showErrorDialog = true,
                                refreshToken = refreshToken
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                            )
                        }
                    }
                }
            }
        }
    }
    
    fun onErrorDialogDismiss() {
        _uiState.update {
            it.copy(showErrorDialog = false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}