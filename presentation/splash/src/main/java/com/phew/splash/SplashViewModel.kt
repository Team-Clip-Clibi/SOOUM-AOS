package com.phew.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phew.core_common.DomainResult
import com.phew.domain.usecase.CheckAppVersion
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
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val usState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        versionCheck()
    }

    private fun versionCheck() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = version(
                CheckAppVersion.Param(
                    appVersion = appVersion,
                    isDebugMode = isDebug
                )
            )) {
                is DomainResult.Failure -> {
                    _uiState.value = UiState.Error
                }

                is DomainResult.Success -> {
                    if (result.data) {
                        _uiState.value = UiState.Success
                        return@launch
                    }
                    _uiState.value = UiState.Fail
                }
            }
        }
    }

}


sealed interface UiState {
    data object Loading : UiState
    data object Success : UiState
    data object Error : UiState
    data object Fail : UiState
}