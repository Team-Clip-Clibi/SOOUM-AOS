package com.phew.presentation.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phew.core_common.exception.asSooumException
import com.phew.domain.orchestrator.SettingsUseCaseOrchestrator
import com.phew.presentation.settings.model.LoginOtherDeviceUiEffect
import com.phew.presentation.settings.model.LoginOtherDeviceUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginOtherDeviceViewModel @Inject constructor(
    private val settingsOrchestrator: SettingsUseCaseOrchestrator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginOtherDeviceUiState())
    val uiState: StateFlow<LoginOtherDeviceUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<LoginOtherDeviceUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

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
            
            settingsOrchestrator.transferCode().fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(
                            code = result.transferCode,
                            expiredAt = result.expiredAt,
                            isCodeGenerated = true,
                            isLoading = false
                        )
                    }
                    startTimer()
                },
                onFailure = { handleApiFailure(it.asSooumException().code) },
            )
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
            _uiEffect.emit(LoginOtherDeviceUiEffect.NavigateBack)
        }
    }

    fun onRetryCodeClick() {
        refreshCodeFromApi()
    }
    
    private fun refreshCodeFromApi() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            settingsOrchestrator.refreshTransferCode().fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(
                            code = result.transferCode,
                            expiredAt = result.expiredAt,
                            isCodeGenerated = true,
                            isLoading = false
                        )
                    }
                    startTimer()
                },
                onFailure = { handleApiFailure(it.asSooumException().code) },
            )
        }
    }
    
    fun onErrorDialogDismiss() {
        _uiState.update {
            it.copy(showErrorDialog = false)
        }
    }

    private suspend fun handleApiFailure(error: Int?) {
        if (error == ERROR_CODE_SERVER) {
            val refreshToken = settingsOrchestrator.refreshToken()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    showErrorDialog = true,
                    refreshToken = refreshToken
                )
            }
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
