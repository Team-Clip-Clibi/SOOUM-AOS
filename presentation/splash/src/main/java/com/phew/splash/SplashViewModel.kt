package com.phew.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phew.core_common.AppVersion
import com.phew.core_common.ERROR
import com.phew.core_common.IsDebug
import com.phew.core_common.errorMessage
import com.phew.domain.model.AppVersionStatusType
import com.phew.domain.usecase.AutoLogin
import com.phew.domain.usecase.CheckAppVersion
import com.phew.domain.usecase.GetFirebaseToken
import com.phew.domain.usecase.SaveNotify
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val version: CheckAppVersion,
    @IsDebug private val isDebug: Boolean,
    @AppVersion private val appVersion: String,
    private val updateFcm: GetFirebaseToken,
    private val notify: SaveNotify,
    private val autoLogin: AutoLogin
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        versionCheck()
    }

    private fun versionCheck() {
        viewModelScope.launch(Dispatchers.IO) {
            version(
                CheckAppVersion.Param(
                    appVersion = appVersion,
                    isDebugMode = isDebug
                )
            ).fold(
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
            updateFcm().fold(
                onSuccess = { _uiState.value = UiState.Success },
                onFailure = { throwable ->
                    _uiState.value = UiState.Error(Result.failure<Unit>(throwable).errorMessage())
                },
            )
        }
    }

    fun saveNotify(data: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            notify(SaveNotify.Param(data)).fold(
                onSuccess = { requestAutoLogin() },
                onFailure = { _uiState.value = UiState.Error(ERROR) },
            )
        }
    }

    private fun requestAutoLogin() {
        viewModelScope.launch(Dispatchers.IO) {
            when (autoLogin()) {
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
