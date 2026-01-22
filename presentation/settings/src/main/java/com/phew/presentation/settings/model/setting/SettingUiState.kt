package com.phew.presentation.settings.model.setting

import com.phew.domain.model.AppVersionStatus
import com.phew.domain.model.AppVersionStatusType
import com.phew.domain.model.RejoinableDate

data class SettingUiState(
    val notificationEnabled: Boolean = true,
    val appVersion: String = "1.10.1",
    val isUpdateAvailable: Boolean = false,
    val isLoading: Boolean = false,
    val settingItems: List<SettingItem> = emptyList(),
    val activityRestrictionDate: String? = null,
    val appVersionStatus: AppVersionStatus? = null,
    val appVersionStatusType: AppVersionStatusType? = null,
    val latestVersion: String? = null,
    val rejoinableDate: RejoinableDate? = null,
    val showWithdrawalDialog: Boolean = false
)