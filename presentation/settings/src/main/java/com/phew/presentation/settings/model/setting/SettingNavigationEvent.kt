package com.phew.presentation.settings.model.setting

sealed class SettingNavigationEvent {
    data object NavigateToLoginOtherDevice : SettingNavigationEvent()
    data object NavigateToLoadPreviousAccount : SettingNavigationEvent()
    data object NavigateToBlockedUsers : SettingNavigationEvent()
    data object NavigateToNotice : SettingNavigationEvent()
    data object NavigateToInquiry : SettingNavigationEvent()
    data object NavigateToPrivacyPolicy : SettingNavigationEvent()
    data object NavigateToAccountDeletion : SettingNavigationEvent()
    data object NavigateToAppStore : SettingNavigationEvent()
}