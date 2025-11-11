package com.phew.presentation.settings.model

sealed class LoginOtherDeviceNavigationEvent {
    data object NavigateBack : LoginOtherDeviceNavigationEvent()
}