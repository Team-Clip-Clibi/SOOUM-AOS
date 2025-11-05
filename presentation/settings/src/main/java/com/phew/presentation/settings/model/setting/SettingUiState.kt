package com.phew.presentation.settings.model.setting

data class SettingUiState(
    val notificationEnabled: Boolean = true,
    val appVersion: String = "1.10.1",
    val isUpdateAvailable: Boolean = false,
    val isLoading: Boolean = false,
    val settingItems: List<SettingItem> = emptyList()
)