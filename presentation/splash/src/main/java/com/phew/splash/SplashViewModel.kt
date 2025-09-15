package com.phew.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val usState: StateFlow<UiState> = _uiState.asStateFlow()

    init{
        versionCheck()
    }

    private fun versionCheck() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = UiState.Fail
        }
    }

}


sealed interface UiState {
    data object Loading : UiState
    data object Success : UiState
    data object Fail : UiState
}