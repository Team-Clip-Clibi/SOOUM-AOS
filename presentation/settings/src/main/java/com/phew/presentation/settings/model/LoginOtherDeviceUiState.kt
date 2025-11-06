package com.phew.presentation.settings.model

data class LoginOtherDeviceUiState(
    val code: String = "",
    val expiredAt: String = "",
    val remainingTimeText: String = "",
    val isRetryEnabled: Boolean = false,
    val isCodeGenerated: Boolean = false,
    val isLoading: Boolean = false
)