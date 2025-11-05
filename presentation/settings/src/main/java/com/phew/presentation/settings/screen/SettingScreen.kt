package com.phew.presentation.settings.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phew.core_design.AppBar.IconLeftAppBar
import com.phew.core_design.NeutralColor
import com.phew.core_design.R
import com.phew.presentation.settings.component.setting.SettingItemRow
import com.phew.presentation.settings.component.setting.SettingToggleRow
import com.phew.presentation.settings.model.setting.SettingNavigationEvent
import com.phew.presentation.settings.model.setting.SettingItem
import com.phew.presentation.settings.model.setting.SettingItemId
import com.phew.presentation.settings.model.setting.SettingItemType
import com.phew.presentation.settings.viewmodel.SettingViewModel
import kotlinx.coroutines.flow.collectLatest
import com.phew.presentation.settings.R as SettingsR

@Composable
fun SettingRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingViewModel = hiltViewModel(),
    onBackPressed: () -> Unit = {},
    onNavigateToLoginOtherDevice: () -> Unit = {},
    onNavigateToLoadPreviousAccount: () -> Unit = {},
    onNavigateToBlockedUsers: () -> Unit = {},
    onNavigateToNotice: () -> Unit = {},
    onNavigateToInquiry: () -> Unit = {},
    onNavigateToPrivacyPolicy: () -> Unit = {},
    onNavigateToAccountDeletion: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                SettingNavigationEvent.NavigateToLoginOtherDevice -> onNavigateToLoginOtherDevice()
                SettingNavigationEvent.NavigateToLoadPreviousAccount -> onNavigateToLoadPreviousAccount()
                SettingNavigationEvent.NavigateToBlockedUsers -> onNavigateToBlockedUsers()
                SettingNavigationEvent.NavigateToNotice -> onNavigateToNotice()
                SettingNavigationEvent.NavigateToInquiry -> onNavigateToInquiry()
                SettingNavigationEvent.NavigateToPrivacyPolicy -> onNavigateToPrivacyPolicy()
                SettingNavigationEvent.NavigateToAccountDeletion -> onNavigateToAccountDeletion()
            }
        }
    }

    SettingScreen(
        modifier = modifier,
        notificationEnabled = uiState.notificationEnabled,
        appVersion = uiState.appVersion,
        isUpdateAvailable = uiState.isUpdateAvailable,
        isLoading = uiState.isLoading,
        settingItems = uiState.settingItems,
        onBackPressed = onBackPressed,
        onNotificationToggle = viewModel::toggleNotification,
        onCheckForUpdates = viewModel::checkForUpdates,
        onLoginOtherDeviceClick = viewModel::onLoginOtherDeviceClick,
        onLoadPreviousAccountClick = viewModel::onLoadPreviousAccountClick,
        onBlockedUsersClick = viewModel::onBlockedUsersClick,
        onNoticeClick = viewModel::onNoticeClick,
        onInquiryClick = viewModel::onInquiryClick,
        onPrivacyPolicyClick = viewModel::onPrivacyPolicyClick,
        onAccountDeletionClick = viewModel::onAccountDeletionClick
    )
}

@Composable
private fun SettingScreen(
    modifier: Modifier = Modifier,
    notificationEnabled: Boolean,
    appVersion: String,
    isUpdateAvailable: Boolean,
    isLoading: Boolean,
    settingItems: List<SettingItem>,
    onBackPressed: () -> Unit,
    onNotificationToggle: (Boolean) -> Unit,
    onCheckForUpdates: () -> Unit,
    onLoginOtherDeviceClick: () -> Unit,
    onLoadPreviousAccountClick: () -> Unit,
    onBlockedUsersClick: () -> Unit,
    onNoticeClick: () -> Unit,
    onInquiryClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onAccountDeletionClick: () -> Unit
) {
    @Composable
    fun getLocalizedTitle(id: SettingItemId): String {
        return when (id) {
            SettingItemId.LOGIN_OTHER_DEVICE -> stringResource(SettingsR.string.setting_login_other_device)
            SettingItemId.LOAD_PREVIOUS_ACCOUNT -> stringResource(SettingsR.string.setting_load_previous_account)
            SettingItemId.BLOCKED_USERS -> stringResource(SettingsR.string.setting_blocked_users)
            SettingItemId.NOTICE -> stringResource(SettingsR.string.setting_notice)
            SettingItemId.INQUIRY -> stringResource(SettingsR.string.setting_inquiry)
            SettingItemId.PRIVACY_POLICY -> stringResource(SettingsR.string.setting_privacy_policy)
            SettingItemId.APP_UPDATE -> stringResource(SettingsR.string.setting_app_update)
            SettingItemId.ACCOUNT_DELETION -> stringResource(SettingsR.string.setting_account_deletion)
        }
    }

    @Composable
    fun getLocalizedSubtitle(id: SettingItemId): String? {
        return when (id) {
            SettingItemId.APP_UPDATE -> "${stringResource(SettingsR.string.setting_app_new_version)} ${stringResource(SettingsR.string.setting_version_format, appVersion)}"
            else -> null
        }
    }

    @Composable
    fun getEndText(id: SettingItemId): String? {
        return when (id) {
            SettingItemId.APP_UPDATE -> if (isUpdateAvailable) stringResource(SettingsR.string.setting_update_available) else null
            else -> null
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NeutralColor.WHITE)
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                IconLeftAppBar(
                    image = R.drawable.ic_left,
                    onClick = onBackPressed,
                    appBarText = stringResource(SettingsR.string.setting_title)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralColor.WHITE)
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
        ) {
            // 1. 알림 설정
            SettingToggleRow(
                title = stringResource(SettingsR.string.setting_notification),
                checked = notificationEnabled,
                onCheckedChange = onNotificationToggle
            )
            
            // 16dp 간격
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(NeutralColor.GRAY_100)
            )
            
            // 2. 다른 기기에서 로그인하기, 이전 계정 불러오기 (그룹)
            val loginOtherDeviceItem = settingItems.find { it.id == SettingItemId.LOGIN_OTHER_DEVICE }
            loginOtherDeviceItem?.let { item ->
                val localizedItem = item.copy(
                    title = getLocalizedTitle(item.id),
                    subtitle = getLocalizedSubtitle(item.id),
                    endText = getEndText(item.id)
                )
                SettingItemRow(
                    item = localizedItem,
                    onClick = onLoginOtherDeviceClick
                )
            }
            
            val loadPreviousAccountItem = settingItems.find { it.id == SettingItemId.LOAD_PREVIOUS_ACCOUNT }
            loadPreviousAccountItem?.let { item ->
                val localizedItem = item.copy(
                    title = getLocalizedTitle(item.id),
                    subtitle = getLocalizedSubtitle(item.id),
                    endText = getEndText(item.id)
                )
                HorizontalDivider(thickness = 1.dp, color = NeutralColor.GRAY_100)
                SettingItemRow(
                    item = localizedItem,
                    onClick = onLoadPreviousAccountClick
                )
            }
            
            // 16dp 간격
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(NeutralColor.GRAY_100)
            )
            
            // 3. 차단 사용자 관리
            val blockedUsersItem = settingItems.find { it.id == SettingItemId.BLOCKED_USERS }
            blockedUsersItem?.let { item ->
                val localizedItem = item.copy(
                    title = getLocalizedTitle(item.id),
                    subtitle = getLocalizedSubtitle(item.id),
                    endText = getEndText(item.id)
                )
                SettingItemRow(
                    item = localizedItem,
                    onClick = onBlockedUsersClick
                )
            }
            
            // 16dp 간격
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(NeutralColor.GRAY_100)
            )
            
            // 4. 공지사항, 문의하기 (그룹)
            val noticeItem = settingItems.find { it.id == SettingItemId.NOTICE }
            noticeItem?.let { item ->
                val localizedItem = item.copy(
                    title = getLocalizedTitle(item.id),
                    subtitle = getLocalizedSubtitle(item.id),
                    endText = getEndText(item.id)
                )
                SettingItemRow(
                    item = localizedItem,
                    onClick = onNoticeClick
                )
            }
            
            val inquiryItem = settingItems.find { it.id == SettingItemId.INQUIRY }
            inquiryItem?.let { item ->
                val localizedItem = item.copy(
                    title = getLocalizedTitle(item.id),
                    subtitle = getLocalizedSubtitle(item.id),
                    endText = getEndText(item.id)
                )
                HorizontalDivider(thickness = 1.dp, color = NeutralColor.GRAY_100)
                SettingItemRow(
                    item = localizedItem,
                    onClick = onInquiryClick
                )
            }
            
            // 16dp 간격
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(NeutralColor.GRAY_100)
            )
            
            // 5. 약관 및 개인정보 처리 동의
            val privacyItem = settingItems.find { it.id == SettingItemId.PRIVACY_POLICY }
            privacyItem?.let { item ->
                val localizedItem = item.copy(
                    title = getLocalizedTitle(item.id),
                    subtitle = getLocalizedSubtitle(item.id),
                    endText = getEndText(item.id)
                )
                SettingItemRow(
                    item = localizedItem,
                    onClick = onPrivacyPolicyClick
                )
            }
            
            // 16dp 간격
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(NeutralColor.GRAY_100)
            )
            
            // 6. 최신버전 업데이트
            val updateItem = settingItems.find { it.id == SettingItemId.APP_UPDATE }
            updateItem?.let { item ->
                val localizedItem = item.copy(
                    title = getLocalizedTitle(item.id),
                    subtitle = getLocalizedSubtitle(item.id),
                    endText = getEndText(item.id)
                )
                SettingItemRow(
                    item = localizedItem,
                    onClick = onCheckForUpdates
                )
            }
            
            // 16dp 간격
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(NeutralColor.GRAY_100)
            )
            
            // 7. 탈퇴하기
            val deletionItem = settingItems.find { it.id == SettingItemId.ACCOUNT_DELETION }
            deletionItem?.let { item ->
                val localizedItem = item.copy(
                    title = getLocalizedTitle(item.id),
                    subtitle = getLocalizedSubtitle(item.id),
                    endText = getEndText(item.id)
                )
                SettingItemRow(
                    item = localizedItem,
                    onClick = onAccountDeletionClick
                )
            }
            
            // 탈퇴하기 밑 여백
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(NeutralColor.GRAY_100)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingScreenPreview() {
    val previewItems = listOf(
        SettingItem(
            id = SettingItemId.LOGIN_OTHER_DEVICE,
            title = "다른 기기에서 로그인하기",
            type = SettingItemType.NAVIGATION
        ),
        SettingItem(
            id = SettingItemId.LOAD_PREVIOUS_ACCOUNT, 
            title = "이전 계정 불러오기",
            type = SettingItemType.NAVIGATION
        ),
        SettingItem(
            id = SettingItemId.BLOCKED_USERS,
            title = "차단 사용자 관리",
            type = SettingItemType.NAVIGATION
        ),
        SettingItem(
            id = SettingItemId.NOTICE,
            title = "공지사항",
            type = SettingItemType.NAVIGATION
        ),
        SettingItem(
            id = SettingItemId.INQUIRY,
            title = "문의하기",
            type = SettingItemType.NAVIGATION
        ),
        SettingItem(
            id = SettingItemId.PRIVACY_POLICY,
            title = "약관 및 개인정보 처리 방침",
            type = SettingItemType.NAVIGATION
        ),
        SettingItem(
            id = SettingItemId.APP_UPDATE,
            title = "최신버전 업데이트",
            subtitle = "최신버전 v1.10.1",
            endText = "업데이트 가능",
            type = SettingItemType.INFO
        ),
        SettingItem(
            id = SettingItemId.ACCOUNT_DELETION,
            title = "탈퇴하기",
            type = SettingItemType.DANGER
        )
    )
    
    SettingScreen(
        notificationEnabled = true,
        appVersion = "1.10.1",
        isUpdateAvailable = true,
        isLoading = false,
        settingItems = previewItems,
        onBackPressed = {},
        onNotificationToggle = {},
        onCheckForUpdates = {},
        onLoginOtherDeviceClick = {},
        onLoadPreviousAccountClick = {},
        onBlockedUsersClick = {},
        onNoticeClick = {},
        onInquiryClick = {},
        onPrivacyPolicyClick = {},
        onAccountDeletionClick = {}
    )
}