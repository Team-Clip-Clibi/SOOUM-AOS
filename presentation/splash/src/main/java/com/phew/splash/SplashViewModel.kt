package com.phew.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR
import com.phew.core_common.IsDebug
import com.phew.core_common.AppVersion
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
    private val notify : SaveNotify,
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
                    _uiState.value = UiState.Error(ERROR)
                }

                is DomainResult.Success -> {
                    if (result.data) {
                        updateFcmToken()
                        return@launch
                    }
                    _uiState.value = UiState.Fail
                }
            }
        }
    }

    private fun updateFcmToken() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = updateFcm()) {
                is DomainResult.Failure -> {
                    _uiState.value = UiState.Error(result.error)
                }

                is DomainResult.Success -> {
                    _uiState.value = UiState.Success
                }
            }
        }
    }

    fun saveNotify(data : Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            when(notify(SaveNotify.Param(data))){
                is DomainResult.Failure ->{
                    _uiState.value = UiState.Error(ERROR)
                }
                is DomainResult.Success -> {
                    _uiState.value = UiState.NextPage
                }
            }
        }
    }

    fun initError(){
        _uiState.value = UiState.Loading
    }
}


sealed interface UiState {
    data object Loading : UiState
    data object Success : UiState
    data object NextPage : UiState
    data class Error(val error: String) : UiState
    data object Fail : UiState
}