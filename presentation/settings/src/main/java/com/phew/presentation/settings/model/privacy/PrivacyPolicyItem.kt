package com.phew.presentation.settings.model.privacy

import com.phew.presentation.settings.model.setting.SettingItemType

data class PrivacyPolicyItem(
    val id: PrivacyPolicyItemId,
    val titleResId: Int,
    val type: SettingItemType = SettingItemType.NAVIGATION
)