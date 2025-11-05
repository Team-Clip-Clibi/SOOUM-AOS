package com.phew.presentation.settings.model.setting

import androidx.annotation.DrawableRes

data class SettingItem(
    val id: SettingItemId,
    val title: String,
    val subtitle: String? = null,
    @DrawableRes val iconRes: Int? = null,
    val type: SettingItemType = SettingItemType.NAVIGATION,
    val hasToggle: Boolean = false,
    val isToggleEnabled: Boolean = false,
    val hasEndText: Boolean = false,
    val endText: String? = null
)

enum class SettingItemType {
    NAVIGATION,
    TOGGLE,
    INFO,
    DANGER
}