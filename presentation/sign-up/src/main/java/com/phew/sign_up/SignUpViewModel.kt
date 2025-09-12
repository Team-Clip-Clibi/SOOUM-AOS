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

    fun agreement(type: String) {
        _uiState.update { state ->
            when (type) {
                AGREEMENT_ALL -> {
                    val newValue = !state.agreementAll
                    state.copy(
                        agreementAll = newValue,
                        agreementService = newValue,
                        agreementLocation = newValue,
                        agreementPersonal = newValue
                    )
                }

                AGREEMENT_SERVICE -> {
                    val newValue = !state.agreementService
                    state.copy(
                        agreementService = newValue
                    ).updateAgreementAll()
                }

                AGREEMENT_LOCATION -> {
                    val newValue = !state.agreementLocation
                    state.copy(
                        agreementLocation = newValue
                    ).updateAgreementAll()
                }

                AGREEMENT_PERSONAL -> {
                    val newValue = !state.agreementPersonal
                    state.copy(
                        agreementPersonal = newValue
                    ).updateAgreementAll()
                }

                else -> state
            }
        }
    }


    private fun SignUp.updateAgreementAll(): SignUp {
        val allChecked = agreementService && agreementLocation && agreementPersonal
        return copy(agreementAll = allChecked)
    }

}

data class SignUp(
    val authCode: String = "",
    val agreementAll: Boolean = false,
    val agreementService: Boolean = false,
    val agreementLocation: Boolean = false,
    val agreementPersonal: Boolean = false
)

sealed interface UiState {
    data object Loading : UiState
    data object Success : UiState
    data class Fail(val errorMessage: String) : UiState
}