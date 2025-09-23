package com.phew.home.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(Home())
    val uiState: StateFlow<Home> = _uiState.asStateFlow()

    fun refresh() {
        if (_uiState.value.refresh is UiState.Loading) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(refresh = UiState.Loading)

            try {
                delay(5000)

                _uiState.value = _uiState.value.copy(refresh = UiState.Success(true))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    refresh = UiState.Fail(e.message ?: "새로고침 실패")
                )
            }
        }
    }
}

data class Home(
    val refresh: UiState<Boolean> = UiState.None,
)

sealed interface UiState<out T> {
    data object None : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Fail(val errorMessage: String) : UiState<Nothing>
}