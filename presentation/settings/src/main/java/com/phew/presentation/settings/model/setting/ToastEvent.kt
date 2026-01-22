package com.phew.presentation.settings.model.setting

sealed class ToastEvent {
    data object ShowCurrentVersionToast : ToastEvent()
    data object ShowNotificationToggleErrorToast : ToastEvent()
}