package com.phew.sign_up

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor() : ViewModel() {
    private var _uiState = MutableStateFlow(SignUp())
    val uiState: StateFlow<SignUp> = _uiState.asStateFlow()


    fun authCode(data: String) {
        _uiState.update { state ->
            state.copy(authCode = data)
        }
    }

}

data class SignUp(
    val authCode: String = ""
)

sealed interface UiState {
    data object Loading : UiState
    data object Success : UiState
    data class Fail(val errorMessage: String) : UiState
}