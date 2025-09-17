package com.phew.sign_up

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phew.core_common.DomainResult
import com.phew.domain.usecase.CreateImageFile
import com.phew.domain.usecase.FinalizePending
import com.phew.domain.usecase.FinishTakePicture
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val createFile : CreateImageFile,
    private val finishPhoto : FinishTakePicture,
    @ApplicationContext private val context: Context
) : ViewModel() {

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
    fun createImage(){
        viewModelScope.launch(Dispatchers.IO) {
            when(val result = createFile()){
                is DomainResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(createImageFile = UiState.Fail(result.error))
                    }
                }
                is DomainResult.Success -> {
                    _uiState.update{ state ->
                        state.copy(createImageFile = UiState.Success(result.data))
                    }
                }
            }
        }
    }

    fun closeFile(data : Uri){
        viewModelScope.launch(Dispatchers.IO) {
            when(val result = finishPhoto(FinishTakePicture.Param(data))){
                is DomainResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(createImageFile = UiState.Fail(result.error))
                    }
                }
                is DomainResult.Success ->{
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
    val agreementService: Boolean = false,
    val agreementLocation: Boolean = false,
    val agreementPersonal: Boolean = false,
    val nickName: String = "",
    val profile: Uri = Uri.EMPTY,
    val profileBottom: Boolean = false,
    val finalizePending: Boolean = false,
    var createImageFile: UiState<Uri> = UiState.Loading
)

sealed interface UiState<out T>{
    data object Loading : UiState<Nothing>
    data class Success<T>(val data : T) : UiState<T>
    data class Fail(val errorMessage: String) : UiState<Nothing>
}