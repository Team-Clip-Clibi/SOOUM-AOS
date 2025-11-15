package com.phew.presentation.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phew.domain.usecase.WithdrawalAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WithdrawalViewModel @Inject constructor(
    private val withdrawalAccount: WithdrawalAccount
): ViewModel() {
    
    private val _uiState = MutableStateFlow(WithdrawalUiState())
    val uiState = _uiState.asStateFlow()
    
    private val _uiEffect = MutableSharedFlow<WithdrawalUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()
    
    fun selectReason(reason: WithdrawalReason) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedReason = reason,
                customReasonText = if (reason == WithdrawalReason.OTHER) currentState.customReasonText else "",
                isWithdrawal = if (reason == WithdrawalReason.OTHER) {
                    currentState.customReasonText.isNotBlank()
                } else {
                    true
                }
            )
        }
    }
    
    fun updateCustomReason(text: String) {
        _uiState.update { currentState ->
            currentState.copy(
                customReasonText = text,
                isWithdrawal = if (currentState.selectedReason == WithdrawalReason.OTHER) {
                    text.isNotBlank()
                } else {
                    currentState.isWithdrawal
                }
            )
        }
    }
    
    fun onWithdrawal() {
        val currentState = _uiState.value
        if (!currentState.isWithdrawal || currentState.selectedReason == null) return
        
        val reason = if (currentState.selectedReason == WithdrawalReason.OTHER) {
            currentState.customReasonText
        } else {
            currentState.selectedReason.key
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            withdrawalAccount(reason)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEffect.emit(WithdrawalUiEffect.ShowSuccessDialog)
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false) }
                    // TODO: 실패 처리 로직
                    _uiEffect.emit(WithdrawalUiEffect.ShowError(exception.message ?: "Unknown error"))
                }
        }
    }
}

data class WithdrawalUiState(
    val selectedReason: WithdrawalReason? = null,
    val customReasonText: String = "",
    val isWithdrawal: Boolean = false,
    val isLoading: Boolean = false
)

enum class WithdrawalReason(val key: String) {
    RARELY_USE("withdrawal_reason_1"),
    NO_DESIRED_FEATURE("withdrawal_reason_2"),
    FREQUENT_ERRORS("withdrawal_reason_3"),
    DIFFICULT_TO_USE("withdrawal_reason_4"),
    CREATE_NEW_ACCOUNT("withdrawal_reason_5"),
    OTHER("withdrawal_reason_6");
    
    companion object {
        fun fromKey(key: String): WithdrawalReason? {
            return values().find { it.key == key }
        }
    }
}

sealed class WithdrawalUiEffect {
    object ShowSuccessDialog : WithdrawalUiEffect()
    data class ShowError(val message: String) : WithdrawalUiEffect()
}