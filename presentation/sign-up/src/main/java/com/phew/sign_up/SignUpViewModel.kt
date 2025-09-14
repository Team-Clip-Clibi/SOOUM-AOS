package com.phew.sign_up

import android.net.Uri
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

    /**
     * 인증 코드 전송
     */
    fun authCode(data: String) {
        _uiState.update { state ->
            state.copy(authCode = data)
        }
    }

    /**
     * 회원가입 동의화면
     */
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

    /**
     * 닉네임
     */
    fun nickName(name: String) {
        _uiState.update { state ->
            state.copy(nickName = name)
        }
    }

    /**
     * 프로필 사진 URL
     */
    fun updateProfile(uri : Uri){
        _uiState.update { state ->
            state.copy(profile = uri)
        }
    }

    /**
     * 프로필 사진 바텀시트 출력 여부
     */
    fun updateProfileBottom(){
        _uiState.update { state ->
            state.copy(profileBottom = !_uiState.value.profileBottom)
        }
    }
}

data class SignUp(
    val authCode: String = "",
    val agreementAll: Boolean = false,
    val agreementService: Boolean = false,
    val agreementLocation: Boolean = false,
    val agreementPersonal: Boolean = false,
    val nickName: String = "",
    val profile: Uri = Uri.EMPTY,
    val profileBottom: Boolean = false
)

sealed interface UiState {
    data object Loading : UiState
    data object Success : UiState
    data class Fail(val errorMessage: String) : UiState
}