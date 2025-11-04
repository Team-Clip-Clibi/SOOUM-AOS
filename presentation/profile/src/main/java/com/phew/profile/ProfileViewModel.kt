package com.phew.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phew.core_common.DomainResult
import com.phew.domain.dto.MyProfileInfo
import com.phew.domain.usecase.GetMyProfileInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getMyProfile: GetMyProfileInfo,
) : ViewModel() {
    private val _uiState = MutableStateFlow(Profile())
    val uiState: StateFlow<Profile> = _uiState.asStateFlow()


    fun myProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { state -> state.copy(myProfileInfo = UiState.Loading) }
            when (val request = getMyProfile()) {
                is DomainResult.Failure -> {
                    _uiState.update { state -> state.copy(myProfileInfo = UiState.Fail(request.error)) }
                }

                is DomainResult.Success -> {
                    _uiState.update { state -> state.copy(myProfileInfo = UiState.Success(request.data)) }
                }
            }
        }
    }
}

data class Profile(
    val myProfileInfo: UiState<MyProfileInfo> = UiState.Loading,
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Fail(val errorMessage: String) : UiState<Nothing>
}