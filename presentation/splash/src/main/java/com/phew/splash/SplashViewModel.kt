package com.phew.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phew.core_common.AppVersion
import com.phew.core_common.ERROR
import com.phew.core_common.IsDebug
import com.phew.core_common.errorMessage
import com.phew.domain.model.AppVersionStatusType
import com.phew.domain.orchestrator.SplashUseCaseOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val splashOrchestrator: SplashUseCaseOrchestrator,
    @IsDebug private val isDebug: Boolean,
    @AppVersion private val appVersion: String,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        versionCheck()
    }

    private fun versionCheck() {
        viewModelScope.launch(Dispatchers.IO) {
            splashOrchestrator.checkAppVersion(appVersion = appVersion, isDebugMode = isDebug).fold(
                onSuccess = { status ->
                    if (status == AppVersionStatusType.UPDATE) {
                        _uiState.value = UiState.Update
                        return@launch
                    }
                    updateFcmToken()
                },
                onFailure = {
                    _uiState.value = UiState.Error(ERROR)
                },
            )
        }
    }

    private fun updateFcmToken() {
        viewModelScope.launch(Dispatchers.IO) {
            splashOrchestrator.updateFcmToken().fold(
                onSuccess = { _uiState.value = UiState.Success },
                onFailure = { throwable ->
                    _uiState.value = UiState.Error(Result.failure<Unit>(throwable).errorMessage())
                },
            )
        }
    }

    fun saveNotify(data: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            splashOrchestrator.saveNotify(data).fold(
                onSuccess = { requestAutoLogin() },
                onFailure = { _uiState.value = UiState.Error(ERROR) },
            )
        }
    }

    private fun requestAutoLogin() {
        viewModelScope.launch(Dispatchers.IO) {
            when (splashOrchestrator.autoLogin()) {
                true -> {
                    _uiState.value = UiState.FeedPage
                }

                false -> {
                    _uiState.value = UiState.SignUpPage
                }
            }
        }
    }

    fun initError() {
        _uiState.value = UiState.Loading
    }
}


sealed interface UiState {
    data object Loading : UiState
    data object Success : UiState
    data object SignUpPage : UiState
    data object FeedPage : UiState
    data object Update : UiState
    data class Error(val error: String) : UiState
}
