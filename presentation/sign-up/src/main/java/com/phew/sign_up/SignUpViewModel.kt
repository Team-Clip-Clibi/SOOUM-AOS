package com.phew.sign_up

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phew.core.ui.model.CameraCaptureRequest
import com.phew.core.ui.model.CameraPickerAction
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.ERROR_TRANSFER_CODE_INVALID
import com.phew.core_common.ERROR_UN_GOOD_IMAGE
import com.phew.core_common.errorMessage
import com.phew.domain.SIGN_UP_ALREADY_SIGN_UP
import com.phew.domain.SIGN_UP_OKAY
import com.phew.domain.SIGN_UP_REGISTERED
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val restoreAccount: RestoreAccount
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUp())
    val uiState: StateFlow<SignUp> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SignUpEffect>()
    val effect: SharedFlow<SignUpEffect> = _effect.asSharedFlow()

    private var checkNickNameJob: Job? = null

    /**
     * 닉네임 초기화 함수
     */
    fun initNickName() {
        checkNickNameJob?.cancel()
        _uiState.update { state ->
            state.copy(
                nickName = "",
                checkNickName = UiState.Loading,
            )
        }
    }

    /**
     * 프로필 사진 초기화 함수
     */
    fun initProfileImage() {
        _uiState.update { state ->
            state.copy(
                profile = listOf(Uri.EMPTY),
            )
        }
    }

    /**
     * 동의 초기화 함수
     */
    fun initAgreement() {
        _uiState.update { state ->
            state.copy(
                agreementAll = false,
                agreedToTermsOfService = false,
                agreedToPrivacyPolicy = false,
                agreedToLocationTerms = false,
            )
        }
    }

    /**
     * 인증 코드 초기화 함수
     */
    fun initAuthCode() {
        _uiState.update { state ->
            state.copy(
                authCode = ""
            )
        }
    }

    /**
     * 닉네임 생성 함수
     */
    fun generateNickName() {
        viewModelScope.launch(Dispatchers.IO) {
            getNickName().fold(
                onSuccess = { nickName ->
                    _uiState.update { state ->
                        state.copy(nickName = nickName, checkNickName = UiState.Success(true))
                    }
                    _effect.emit(SignUpEffect.NavigateToNickName)
                },
                onFailure = {
                    _effect.emit(SignUpEffect.ShowError(SignUpError.Network))
                }
            )
        }
    }

    /**
     * 회원가입
     */
    fun signUp() {
        viewModelScope.launch(Dispatchers.IO) {
            requestSignUp(
                data = RequestSignUp.Param(
                    agreedToLocationTerms = _uiState.value.agreedToLocationTerms,
                    agreedToPrivacyPolicy = _uiState.value.agreedToPrivacyPolicy,
                    agreedToTermsOfService = _uiState.value.agreedToTermsOfService,
                    nickName = _uiState.value.nickName,
                    profileImage = uiState.value.profile.lastOrNull()?.toString() ?: Uri.EMPTY.toString()
                )
            ).fold(
                onSuccess = {
                    _effect.emit(SignUpEffect.NavigateToFinish)
                },
                onFailure = { throwable ->
                    when (throwable.toUiMessage()) {
                        ERROR_NETWORK -> {
                            _effect.emit(SignUpEffect.ShowError(SignUpError.Network))
                        }

                        ERROR_UN_GOOD_IMAGE -> {
                            setImageDialog(true)
                        }

                        else -> {
                            _effect.emit(SignUpEffect.ShowError(SignUpError.App))
                        }
                    }
                }
            )
        }
    }

    /**
     * 닉네임 검증 함수
     */
    private suspend fun checkName(name: String) {
        withContext(Dispatchers.IO) { checkNickName(CheckNickName.Param(name)) }.fold(
            onSuccess = { available ->
                _uiState.update { state ->
                    if (state.nickName == name) {
                        state.copy(checkNickName = UiState.Success(available))
                    } else {
                        state
                    }
                }
            },
            onFailure = { throwable ->
                if (_uiState.value.nickName == name) {
                    _uiState.update { state ->
                        state.copy(checkNickName = UiState.Fail(throwable.toUiMessage()))
                    }
                    _effect.emit(SignUpEffect.ShowError(SignUpError.Network))
                }
            }
        )
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
            restoreAccount(RestoreAccount.Param(_uiState.value.authCode.trim())).fold(
                onSuccess = {
                    _effect.emit(SignUpEffect.RestoreSuccess)
                },
                onFailure = { throwable ->
                    val error = when (throwable.toUiMessage()) {
                        ERROR_NETWORK -> SignUpError.Network
                        ERROR_TRANSFER_CODE_INVALID -> SignUpError.InvalidAuthCode
                        else -> SignUpError.App
                    }
                    _effect.emit(SignUpEffect.ShowError(error))
                }
            )
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
    fun checkRegister() {
        viewModelScope.launch(Dispatchers.IO) {
            checkSignUp().fold(
                onSuccess = { checkResult ->
                    val signUpResult = SignUpResult(
                        time = checkResult.second,
                        result = checkResult.first
                    )
                    when (signUpResult.result) {
                        SIGN_UP_OKAY -> _effect.emit(SignUpEffect.NavigateToAgreement)
                        SIGN_UP_REGISTERED, SIGN_UP_ALREADY_SIGN_UP -> login()
                        else -> _effect.emit(SignUpEffect.ShowDialog(signUpResult))
                    }
                },
                onFailure = {
                    _effect.emit(SignUpEffect.ShowError(SignUpError.Network))
                }
            )
        }
    }

    /**
     * 로그인
     */
    private fun login() {
        viewModelScope.launch(Dispatchers.IO) {
            requestLogin().fold(
                onSuccess = {
                    _effect.emit(SignUpEffect.NavigateHome)
                },
                onFailure = {
                    _effect.emit(SignUpEffect.ShowError(SignUpError.Network))
                }
            )
        }
    }

    /**
     * 닉네임
     */
    fun nickName(name: String) {
        _uiState.update { state ->
            state.copy(
                nickName = name,
                checkNickName = UiState.Loading
            )
        }

        checkNickNameJob?.cancel()
        if (name.trim().length !in 2..8) {
            return
        }

        checkNickNameJob = viewModelScope.launch {
            delay(NICK_NAME_CHECK_DELAY_MILLIS)
            checkName(name)
        }
    }

    /**
     * 프로필 사진 URL
     */
    private fun updateProfile(uri: Uri) {
        _uiState.update { state ->
            state.copy(profile = state.profile + uri)
        }
    }

    /**
     * 프로필 사진 바텀시트 출력 여부
     */
    fun updateProfileBottom() {
        _uiState.update { state ->
            state.copy(profileBottom = !state.profileBottom)
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

            CameraPickerAction.Default -> {
                _uiState.update { state ->
                    state.copy(
                        profile = listOf(Uri.EMPTY),
                        profileBottom = false
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

    fun onProfileCameraCaptureLaunched() {
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
            createFile().fold(
                onSuccess = { uri ->
                    _uiState.update { state ->
                        state.copy(
                            pendingProfileCameraCapture = CameraCaptureRequest(
                                id = System.currentTimeMillis(),
                                uri = uri
                            )
                        )
                    }
                },
                onFailure = {
                    // 실패 시 별도 처리 없음
                }
            )
        }
    }

    /**
     * 사진 만들기 종료
     */
    private fun closeFile(data: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            finishPhoto(FinishTakePicture.Param(data)).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(profile = state.profile + data)
                    }
                },
                onFailure = {
                    // 실패 시 별도 처리 없음
                }
            )
        }
    }

    fun setImageDialog(result: Boolean) {
        _uiState.update { state ->
            state.copy(
                imageDialog = result,
                profile = if (!result) {
                    if (state.profile.size > 1) {
                        state.profile.dropLast(1)
                    } else {
                        listOf(Uri.EMPTY)
                    }
                } else state.profile,
            )
        }
    }

    fun loadPolicyView(isStart: Boolean) {
        _uiState.update { state -> state.copy(loadPolicyView = isStart) }
    }

}

data class SignUp(
    val authCode: String = "",
    val agreementAll: Boolean = false,
    val agreedToTermsOfService: Boolean = false,
    val agreedToLocationTerms: Boolean = false,
    val agreedToPrivacyPolicy: Boolean = false,
    val nickName: String = "",
    val profile: List<Uri> = listOf(Uri.EMPTY),
    val profileBottom: Boolean = false,
    val shouldLaunchProfileAlbum: Boolean = false,
    val shouldRequestProfileCameraPermission: Boolean = false,
    val pendingProfileCameraCapture: CameraCaptureRequest? = null,
    val checkNickName: UiState<Boolean> = UiState.Loading,
    val imageDialog: Boolean = false,
    val loadPolicyView: Boolean = false
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Fail(val errorMessage: String) : UiState<Nothing>
}

sealed interface SignUpEffect {
    data object NavigateToAgreement : SignUpEffect
    data object NavigateHome : SignUpEffect
    data object NavigateToNickName : SignUpEffect
    data object NavigateToFinish : SignUpEffect
    data object RestoreSuccess : SignUpEffect
    data class ShowDialog(val result: SignUpResult) : SignUpEffect
    data class ShowError(val error: SignUpError) : SignUpEffect
}

enum class SignUpError {
    Network,
    InvalidAuthCode,
    App
}

private fun Throwable.toUiMessage(): String = Result.failure<Unit>(this).errorMessage()

private const val NICK_NAME_CHECK_DELAY_MILLIS = 350L
