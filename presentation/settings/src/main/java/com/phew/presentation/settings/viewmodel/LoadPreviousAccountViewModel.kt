package com.phew.presentation.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phew.domain.usecase.TransferAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TransferAccountEvent {
    data object Success : TransferAccountEvent()
    data class Error(val message: String) : TransferAccountEvent()
}

data class LoadPreviousAccountUiState(
    val isLoading: Boolean = false,
    val transferEvent: TransferAccountEvent? = null
)

@HiltViewModel
class LoadPreviousAccountViewModel @Inject constructor(
    private val transferAccount: TransferAccount
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoadPreviousAccountUiState())
    val uiState: StateFlow<LoadPreviousAccountUiState> = _uiState.asStateFlow()
    
    fun transferAccount(transferCode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, transferEvent = null)
            
            transferAccount(TransferAccount.Param(transferCode))
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        transferEvent = TransferAccountEvent.Success
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        transferEvent = TransferAccountEvent.Error(
                            throwable.message ?: "계정 이전에 실패했습니다."
                        )
                    )
                }
        }
    }
    
    fun clearTransferEvent() {
        _uiState.value = _uiState.value.copy(transferEvent = null)
    }
}