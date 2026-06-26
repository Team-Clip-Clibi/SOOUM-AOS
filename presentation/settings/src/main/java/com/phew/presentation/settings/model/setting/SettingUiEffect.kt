package com.phew.presentation.settings.model.setting

sealed interface SettingUiEffect {
    data object NavigateToLoginOtherDevice : SettingUiEffect
    data object NavigateToLoadPreviousAccount : SettingUiEffect
    data object NavigateToBlockedUsers : SettingUiEffect
    data object NavigateToNotice : SettingUiEffect
    data object NavigateToPrivacyPolicy : SettingUiEffect
    data object NavigateToAccountDeletion : SettingUiEffect
    data object NavigateToAppStore : SettingUiEffect
    data object NavigateToAlarm : SettingUiEffect
    data class SendInquiryMail(val refreshToken: String) : SettingUiEffect
    data object ShowCurrentVersionToast : SettingUiEffect
    data object ShowNotificationToggleErrorToast : SettingUiEffect
}
