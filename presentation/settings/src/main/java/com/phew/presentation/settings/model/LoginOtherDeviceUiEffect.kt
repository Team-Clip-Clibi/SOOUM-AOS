package com.phew.presentation.settings.model

sealed interface LoginOtherDeviceUiEffect {
    data object NavigateBack : LoginOtherDeviceUiEffect
}
