package com.phew.sign_up

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phew.core.ui.model.CameraCaptureRequest
import com.phew.core.ui.model.CameraPickerAction
import com.phew.core_common.DomainResult
import com.phew.domain.usecase.CheckNickName
import com.phew.domain.usecase.CheckSignUp
import com.phew.domain.usecase.CreateImageFile
import com.phew.domain.usecase.FinishTakePicture
import com.phew.domain.usecase.GetNickName
import com.phew.domain.usecase.Login
import com.phew.domain.usecase.RequestSignUp
import com.phew.domain.usecase.RestoreAccount
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
    private val createFile: CreateImageFile,
    private val finishPhoto: FinishTakePicture,
    private val checkSignUp: CheckSignUp,
    private val requestLogin: Login,
    private val getNickName: GetNickName,
    private val requestSignUp: RequestSignUp,
    private val checkNickName: CheckNickName,
    private val restoreAccount : RestoreAccount
) : ViewModel() {

    private var _uiState = MutableStateFlow(SignUp())
    val uiState: StateFlow<SignUp> = _uiState.asStateFlow()

    init {
        generateNickName()
        checkRegister()
    }

    /**
     * 닉네임 생성 함수
     */
    private fun generateNickName() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = getNickName()) {
                is DomainResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(nickName = "ERROR")
                    }
                }

                is DomainResult.Success -> {
                    _uiState.update { state ->
                        state.copy(nickName = result.data)

                    }
                }
            }
        }
    }

    /**
     * 회원가입
     */
    fun signUp() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = requestSignUp(
                data = RequestSignUp.Param(
                    agreedToLocationTerms = _uiState.value.agreedToLocationTerms,
                    agreedToPrivacyPolicy = _uiState.value.agreedToPrivacyPolicy,
                    agreedToTermsOfService = _uiState.value.agreedToTermsOfService,
                    nickName = _uiState.value.nickName,
                    profileImage = _uiState.value.profile.toString()
                )
            )) {
                is DomainResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(signUp = UiState.Fail(result.error))
                    }
                }

                is DomainResult.Success -> {
                    _uiState.update { state ->
                        state.copy(signUp = UiState.Success(Unit))
                    }
                }
            }
        }
    }

    /**
     * 닉네임 검증 함수
     */
    private fun checkName() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = checkNickName(CheckNickName.Param(_uiState.value.nickName))) {
                is DomainResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(checkNickName = UiState.Fail(result.error))
                    }
                }

                is DomainResult.Success -> {
                    _uiState.update { state ->
                        state.copy(checkNickName = UiState.Success(result.data))
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
     * 다른 기기에 있는 계정 가져오기
     */
    fun restoreAccount() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = restoreAccount(RestoreAccount.Param(_uiState.value.authCode.trim()))) {
                is DomainResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(restoreAccountResult = UiState.Fail(result.error))
                    }
                }

                is DomainResult.Success -> {
                    _uiState.update { state ->
                        state.copy(restoreAccountResult = UiState.Success(Unit))
                    }
                }
            }
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
    private fun checkRegister() {
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
                            ),
                        )
                    }
                }
            }
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
     * 닉네임 중복 검사 여부 초기화
     */
    fun initNickName() {
        _uiState.update { state ->
            state.copy(checkNickName = UiState.Loading)
        }
    }

    /**
     * 닉네임
     */
    fun nickName(name: String) {
        _uiState.update { state ->
            state.copy(nickName = name)
        }
        checkName()
    }

    /**
     * 프로필 사진 URL
     */
    private fun updateProfile(uri: Uri) {
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

    fun onProfilePickerAction(action: CameraPickerAction) {
        when (action) {
            CameraPickerAction.Album -> {
                _uiState.update { state ->
                    state.copy(
                        profileBottom = false,
                        shouldLaunchProfileAlbum = true
                    )
                }
            }

            CameraPickerAction.Camera -> {
                _uiState.update { state ->
                    state.copy(
                        profileBottom = false,
                        shouldRequestProfileCameraPermission = true
                    )
                }
            }
        }
    }

    fun onProfileAlbumRequestConsumed() {
        _uiState.update { state ->
            state.copy(shouldLaunchProfileAlbum = false)
        }
    }

    fun onProfileCameraPermissionRequestConsumed() {
        _uiState.update { state ->
            state.copy(shouldRequestProfileCameraPermission = false)
        }
    }

    fun onProfileCameraPermissionResult(granted: Boolean) {
        if (granted) {
            createImage()
        }
    }

    fun onProfileCameraCaptureLaunched(request: CameraCaptureRequest) {
        _uiState.update { state ->
            state.copy(pendingProfileCameraCapture = null)
        }
    }

    fun onProfileCameraCaptureResult(success: Boolean, uri: Uri) {
        if (success) {
            closeFile(uri)
        }
    }

    fun onAlbumImagePicked(uri: Uri) {
        updateProfile(uri)
    }


    /**
     * 이미지 파일 생성기
     */
    private fun createImage() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = createFile()) {
                is DomainResult.Failure -> {
                    // 실패 시 별도 처리 없음
                }

                is DomainResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            pendingProfileCameraCapture = CameraCaptureRequest(
                                id = System.currentTimeMillis(),
                                uri = result.data
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * 사진 만들기 종료
     */
    private fun closeFile(data: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = finishPhoto(FinishTakePicture.Param(data))) {
                is DomainResult.Failure -> {
                    // 실패 시 별도 처리 없음
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
    val profile: Uri = Uri.EMPTY,
    val profileBottom: Boolean = false,
    val shouldLaunchProfileAlbum: Boolean = false,
    val shouldRequestProfileCameraPermission: Boolean = false,
    val pendingProfileCameraCapture: CameraCaptureRequest? = null,
    val finalizePending: Boolean = false,
    val checkSignUp: UiState<SignUpResult> = UiState.Loading,
    val checkNickName: UiState<Boolean> = UiState.Loading,
    val login: UiState<Unit> = UiState.Loading,
    val restoreAccountResult : UiState<Unit> = UiState.Loading,
    val signUp: UiState<Unit> = UiState.Loading,
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Fail(val errorMessage: String) : UiState<Nothing>
}
