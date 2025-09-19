package com.phew.sign_up

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phew.core_common.DomainResult
import com.phew.domain.usecase.CheckSignUp
import com.phew.domain.usecase.CreateImageFile
import com.phew.domain.usecase.FinalizePending
import com.phew.domain.usecase.FinishTakePicture
import com.phew.domain.usecase.GetNickName
import com.phew.domain.usecase.Login
import com.phew.sign_up.dto.SignUpResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val finalizePending: FinalizePending,
    private val createFile: CreateImageFile,
    private val finishPhoto: FinishTakePicture,
    private val checkSignUp: CheckSignUp,
    private val requestLogin: Login,
    private val nickName: GetNickName,
) : ViewModel() {

    private var _uiState = MutableStateFlow(SignUp())
    val uiState: StateFlow<SignUp> = _uiState.asStateFlow()
    init {
        generateNickName()
    }
    private fun generateNickName() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = nickName()) {
                is DomainResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(nickNameHint = UiState.Fail(result.error))
                    }
                }

                is DomainResult.Success -> {
                    _uiState.update { state ->
                        state.copy(nickNameHint = UiState.Success(result.data))

                    }
                }
            }
        }
    }

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
                        agreedToTermsOfService = newValue,
                        agreedToLocationTerms = newValue,
                        agreedToPrivacyPolicy = newValue
                    )
                }

                AGREEMENT_SERVICE -> {
                    val newValue = !state.agreedToTermsOfService
                    state.copy(
                        agreedToTermsOfService = newValue
                    ).updateAgreementAll()
                }

                AGREEMENT_LOCATION -> {
                    val newValue = !state.agreedToLocationTerms
                    state.copy(
                        agreedToLocationTerms = newValue
                    ).updateAgreementAll()
                }

                AGREEMENT_PERSONAL -> {
                    val newValue = !state.agreedToPrivacyPolicy
                    state.copy(
                        agreedToPrivacyPolicy = newValue
                    ).updateAgreementAll()
                }

                else -> state
            }
        }
    }

    private fun SignUp.updateAgreementAll(): SignUp {
        val allChecked = agreedToTermsOfService && agreedToLocationTerms && agreedToPrivacyPolicy
        return copy(agreementAll = allChecked)
    }

    /**
     * 회원 가입 가능 여부 확인
     */
    fun signUp() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = checkSignUp()) {
                is DomainResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            checkSignUp = UiState.Fail(result.error)
                        )
                    }
                }

                is DomainResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            checkSignUp = UiState.Success(
                                SignUpResult(
                                    time = result.data.second,
                                    result = result.data.first
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    fun initCheckSignUp() {
        _uiState.update { state ->
            state.copy(checkSignUp = UiState.Loading)
        }
    }

    /**
     * 로그인
     */
    fun login() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = requestLogin()) {
                is DomainResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            login = UiState.Fail(result.error)
                        )
                    }
                }

                is DomainResult.Success -> {
                    _uiState.update { state ->
                        state.copy(login = UiState.Success(Unit))
                    }
                }
            }
        }
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
    fun updateProfile(uri: Uri) {
        _uiState.update { state ->
            state.copy(profile = uri)
        }
    }

    /**
     * 프로필 사진 바텀시트 출력 여부
     */
    fun updateProfileBottom() {
        _uiState.update { state ->
            state.copy(profileBottom = !_uiState.value.profileBottom)
        }
    }


    /**
     * 이미지 파일 생성기
     */
    fun createImage() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = createFile()) {
                is DomainResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(createImageFile = UiState.Fail(result.error))
                    }
                }

                is DomainResult.Success -> {
                    _uiState.update { state ->
                        state.copy(createImageFile = UiState.Success(result.data))
                    }
                }
            }
        }
    }

    fun closeFile(data: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = finishPhoto(FinishTakePicture.Param(data))) {
                is DomainResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(createImageFile = UiState.Fail(result.error))
                    }
                }

                is DomainResult.Success -> {
                    _uiState.update { state ->
                        state.copy(profile = result.data)
                    }
                }
            }
        }
    }

}

data class SignUp(
    val authCode: String = "",
    val agreementAll: Boolean = false,
    val agreedToTermsOfService: Boolean = false,
    val agreedToLocationTerms: Boolean = false,
    val agreedToPrivacyPolicy: Boolean = false,
    val nickName: String = "",
    val nickNameHint: UiState<String> = UiState.Loading,
    val profile: Uri = Uri.EMPTY,
    val profileBottom: Boolean = false,
    val finalizePending: Boolean = false,
    var createImageFile: UiState<Uri> = UiState.Loading,
    val checkSignUp: UiState<SignUpResult> = UiState.Loading,
    val login: UiState<Unit> = UiState.Loading,
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Fail(val errorMessage: String) : UiState<Nothing>
}