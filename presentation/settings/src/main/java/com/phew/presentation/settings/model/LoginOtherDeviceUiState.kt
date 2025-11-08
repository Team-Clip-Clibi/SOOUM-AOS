package com.phew.presentation.settings.model

data class LoginOtherDeviceUiState(
    val code: String = "",
    val expiredAt: String = "",
    val remainingTimeText: String = "",
    val isCodeGenerated: Boolean = false,
    val isLoading: Boolean = false
)