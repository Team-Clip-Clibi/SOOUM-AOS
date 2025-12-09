package com.phew.presentation.settings.screen

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.phew.core.ui.util.InquiryUtils
import com.phew.core_common.TimeUtils
import com.phew.core_design.AppBar.IconLeftAppBar
import com.phew.core_design.DialogComponent
import com.phew.core_design.NeutralColor
import com.phew.core_design.R
import com.phew.core_design.TextComponent
import com.phew.core_design.component.toast.SooumToast
import com.phew.presentation.settings.component.setting.SettingItemRow
import com.phew.presentation.settings.component.setting.SettingToggleRow
import com.phew.presentation.settings.model.setting.SettingItem
import com.phew.presentation.settings.model.setting.SettingItemId
import com.phew.presentation.settings.model.setting.SettingItemType
import com.phew.presentation.settings.model.setting.SettingNavigationEvent
import com.phew.presentation.settings.model.setting.ToastEvent
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
    onNavigateToPrivacyPolicy: () -> Unit = {},
    onNavigateToAccountDeletion: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                SettingNavigationEvent.NavigateToLoginOtherDevice -> onNavigateToLoginOtherDevice()
                SettingNavigationEvent.NavigateToLoadPreviousAccount -> onNavigateToLoadPreviousAccount()
                SettingNavigationEvent.NavigateToBlockedUsers -> onNavigateToBlockedUsers()
                SettingNavigationEvent.NavigateToNotice -> onNavigateToNotice()
                SettingNavigationEvent.NavigateToPrivacyPolicy -> onNavigateToPrivacyPolicy()
                SettingNavigationEvent.NavigateToAccountDeletion -> onNavigateToAccountDeletion()
                SettingNavigationEvent.NavigateToAppStore -> {
                    openAppStore(context)
                }
                is SettingNavigationEvent.SendInquiryMail -> {
                    InquiryUtils.openInquiryMail(
                        context = context,
                        refreshToken = event.refreshToken
                    )
                }
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.toastEvent.collectLatest { event ->
            when (event) {
                ToastEvent.ShowCurrentVersionToast -> {
                    SooumToast.makeToast(
                        context,
                        context.getString(SettingsR.string.setting_current_new_version),
                        SooumToast.LENGTH_SHORT
                    ).show()
                }
                ToastEvent.ShowNotificationToggleErrorToast -> {
                    SooumToast.makeToast(
                        context,
                        context.getString(SettingsR.string.setting_alarm_failed),
                        SooumToast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.ic_refresh)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = uiState.isLoading
    )

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(44.dp)
            )
        }
    }

    SettingScreen(
        modifier = modifier,
        notificationEnabled = uiState.notificationEnabled,
        appVersion = uiState.appVersion,
        isUpdateAvailable = uiState.isUpdateAvailable,
        settingItems = uiState.settingItems,
        activityRestrictionDate = uiState.activityRestrictionDate,
        latestVersion = uiState.latestVersion,
        onBackPressed = onBackPressed,
        onNotificationToggle = viewModel::onNotificationToggle,
        onLoginOtherDeviceClick = viewModel::onLoginOtherDeviceClick,
        onLoadPreviousAccountClick = viewModel::onLoadPreviousAccountClick,
        onBlockedUsersClick = viewModel::onBlockedUsersClick,
        onNoticeClick = viewModel::onNoticeClick,
        onInquiryClick = viewModel::onInquiryClick,
        onPrivacyPolicyClick = viewModel::onPrivacyPolicyClick,
        onAccountDeletionClick = viewModel::onAccountDeletionClick,
        onAppUpdateClick = viewModel::onAppUpdateClick
    )
    
    // 탈퇴 확인 다이얼로그
    if (uiState.showWithdrawalDialog) {
        val rejoinableDate = uiState.rejoinableDate
        val dialogMessage = if (rejoinableDate?.isActivityRestricted == true) {
            stringResource(SettingsR.string.setting_withdrawal_dialog_rejoin_date, 
                TimeUtils.formatToWithdrawalDate(rejoinableDate.rejoinableDate))
        } else {
            stringResource(SettingsR.string.setting_withdrawal_dialog_rejoin_seven_date)
        }
        
        DialogComponent.DefaultButtonTwo(
            title = stringResource(SettingsR.string.setting_withdrawal_dialog_title),
            description = dialogMessage,
            buttonTextStart = stringResource(SettingsR.string.setting_withdrawal_dialog_cancel),
            buttonTextEnd = stringResource(SettingsR.string.setting_withdrawal_dialog_ok),
            onClick = viewModel::onConfirmWithdrawal,
            onDismiss = viewModel::onDismissWithdrawalDialog,
            startButtonTextColor = NeutralColor.BLACK
        )
    }
}

@Composable
private fun SettingScreen(
    modifier: Modifier = Modifier,
    notificationEnabled: Boolean,
    appVersion: String,
    isUpdateAvailable: Boolean,
    settingItems: List<SettingItem>,
    activityRestrictionDate: String?,
    latestVersion: String?,
    onBackPressed: () -> Unit,
    onNotificationToggle: (Boolean) -> Unit,
    onLoginOtherDeviceClick: () -> Unit,
    onLoadPreviousAccountClick: () -> Unit,
    onBlockedUsersClick: () -> Unit,
    onNoticeClick: () -> Unit,
    onInquiryClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onAccountDeletionClick: () -> Unit,
    onAppUpdateClick: () -> Unit
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
            SettingItemId.APP_UPDATE -> latestVersion?.let { version ->
                "${stringResource(SettingsR.string.setting_app_new_version)} ${
                    stringResource(
                        SettingsR.string.setting_version_format,
                        version
                    )
                }"
            } ?: "${stringResource(SettingsR.string.setting_app_new_version)} ${
                stringResource(
                    SettingsR.string.setting_version_format,
                    appVersion
                )
            }"

            else -> null
        }
    }

    @Composable
    fun getEndText(id: SettingItemId): String? {
        return when (id) {
            SettingItemId.APP_UPDATE -> appVersion
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
            val loginOtherDeviceItem =
                settingItems.find { it.id == SettingItemId.LOGIN_OTHER_DEVICE }
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

            val loadPreviousAccountItem =
                settingItems.find { it.id == SettingItemId.LOAD_PREVIOUS_ACCOUNT }
            loadPreviousAccountItem?.let { item ->
                val localizedItem = item.copy(
                    title = getLocalizedTitle(item.id),
                    subtitle = getLocalizedSubtitle(item.id),
                    endText = getEndText(item.id)
                )
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
                    onClick = onAppUpdateClick
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

            // 8. 이용제한 안내 (activityRestrictionDate가 있을 때만 표시)
            activityRestrictionDate?.let { date ->
                // 16dp 간격
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .background(NeutralColor.GRAY_100)
                )

                // 이용제한 안내
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeutralColor.GRAY_100)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = stringResource(SettingsR.string.activity_restriction_title),
                        style = TextComponent.CAPTION_1_SB_12,
                        color = NeutralColor.BLACK
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(SettingsR.string.activity_restriction_guide_message),
                        style = TextComponent.CAPTION_3_M_10,
                        color = NeutralColor.GRAY_500
                    )
                    Text(
                        text = stringResource(SettingsR.string.activity_restriction_message, date),
                        style = TextComponent.CAPTION_3_M_10,
                        color = NeutralColor.GRAY_500
                    )
                }
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
        settingItems = previewItems,
        activityRestrictionDate = "2024년 12월 25일 14시 30분",
        onBackPressed = {},
        onNotificationToggle = {},
        onLoginOtherDeviceClick = {},
        onLoadPreviousAccountClick = {},
        onBlockedUsersClick = {},
        onNoticeClick = {},
        onInquiryClick = {},
        onPrivacyPolicyClick = {},
        onAccountDeletionClick = {},
        onAppUpdateClick = {},
        latestVersion = ""
    )
}

private fun openAppStore(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "market://details?id=${context.packageName}".toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data =
                    "https://play.google.com/store/apps/details?id=${context.packageName}".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

