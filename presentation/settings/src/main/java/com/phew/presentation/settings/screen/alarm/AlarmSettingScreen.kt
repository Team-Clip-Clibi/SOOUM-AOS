package com.phew.presentation.settings.screen.alarm

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phew.core_design.AppBar
import com.phew.core_design.NeutralColor
import com.phew.core_design.TextComponent
import com.phew.domain.dto.Alarm
import com.phew.presentation.settings.viewmodel.SettingViewModel
import com.phew.presentation.settings.R
import com.phew.presentation.settings.component.setting.AlarmView
import com.phew.presentation.settings.component.setting.AlarmViewWithSubTitle
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.phew.core_design.DialogComponent

@Composable
internal fun AlarmSettingScreen(
    viewModel: SettingViewModel = hiltViewModel(),
    onBackPressed: () -> Unit
) {
    val uisState by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler(onBack = onBackPressed)
    LaunchedEffect(Unit) {
        viewModel.loadNotificationState()
    }
    Scaffold(
        topBar = {
            AppBar.IconLeftAppBar(
                onClick = remember(onBackPressed) {
                    {
                        onBackPressed()
                    }
                },
                appBarText = stringResource(R.string.setting_title)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = NeutralColor.WHITE)
                .padding(
                    top = paddingValues.calculateTopPadding() + 16.dp,
                    bottom = paddingValues.calculateBottomPadding()
                )
                .verticalScroll(rememberScrollState())
        ) {
            ServiceAlarmView(
                onValueChange = { result ->
                    viewModel.onNotificationToggle(result)
                },
                value = uisState.notificationEnabled
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(color = NeutralColor.GRAY_100)
            )

            EventAlarm(
                onValueChange = { result ->
                    viewModel.onNotificationToggle(result)
                },
                value = uisState.notificationEnabled
            )
        }
    }
}

@Composable
private fun ServiceAlarmView(
    onValueChange: (Alarm) -> Unit,
    value: Alarm
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.alarm_view_title),
            style = TextComponent.CAPTION_1_SB_12,
            color = NeutralColor.GRAY_400,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
        )
        AlarmView(
            title = stringResource(R.string.alarm_view_item_write_comment),
            isActivate = value.commentCardNotify,
            onClick = { result ->
                val updatedAlarm = value.copy(commentCardNotify = result)
                onValueChange(updatedAlarm)
            }
        )
        AlarmView(
            title = stringResource(R.string.alarm_view_item_write_like),
            isActivate = value.cardLikeNotify,
            onClick = { result ->
                val updatedAlarm = value.copy(cardLikeNotify = result)
                onValueChange(updatedAlarm)
            }
        )
        AlarmView(
            title = stringResource(R.string.alarm_view_item_follow_new_card),
            isActivate = value.followUserCardNotify,
            onClick = { result ->
                val updatedAlarm = value.copy(followUserCardNotify = result)
                onValueChange(updatedAlarm)
            }
        )
        AlarmView(
            title = stringResource(R.string.alarm_view_item_new_follow),
            isActivate = value.newFollowerNotify,
            onClick = { result ->
                val updatedAlarm = value.copy(newFollowerNotify = result)
                onValueChange(updatedAlarm)
            }
        )
        AlarmView(
            title = stringResource(R.string.alarm_view_item_search_new_card_comment),
            isActivate = value.cardNewCommentNotify,
            onClick = { result ->
                val updatedAlarm = value.copy(cardNewCommentNotify = result)
                onValueChange(updatedAlarm)
            }
        )
        AlarmView(
            title = stringResource(R.string.alarm_view_item_recommend_card),
            isActivate = value.recommendedContentNotify,
            onClick = { result ->
                val updatedAlarm = value.copy(recommendedContentNotify = result)
                onValueChange(updatedAlarm)
            }
        )
        AlarmViewWithSubTitle(
            title = stringResource(R.string.alarm_view_item_tag),
            subTitle = stringResource(R.string.alarm_view_item_tag_sub_title),
            isActivate = value.favoriteTagNotify,
            onClick = { result ->
                val updatedAlarm = value.copy(favoriteTagNotify = result)
                onValueChange(updatedAlarm)
            }
        )
        AlarmViewWithSubTitle(
            title = stringResource(R.string.alarm_view_item_banned_info),
            subTitle = stringResource(R.string.alarm_view_item_banned_info_sub_title),
            isActivate = value.policyViolationNotify,
            onClick = { result ->
                val updatedAlarm = value.copy(policyViolationNotify = result)
                onValueChange(updatedAlarm)
            }
        )
    }
}

@Composable
private fun EventAlarm(
    value: Alarm,
    onValueChange: (Alarm) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.alarm_view_item_new_event_title),
            style = TextComponent.CAPTION_1_SB_12,
            color = NeutralColor.GRAY_400,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp)
        )
        AlarmView(
            title = stringResource(R.string.alarm_view_item_new_event),
            isActivate = value.serviceUpdateNotify,
            onClick = { result ->
                if(!result){
                    showDialog = true
                }else{
                    val updatedAlarm = value.copy(serviceUpdateNotify = true)
                    onValueChange(updatedAlarm)
                }

            }
        )
    }
    if(showDialog){
        EventAlarmOffDialog(
            onUserClick = {isConfirmed ->
                showDialog = false
                if (isConfirmed) {
                    val updatedAlarm = value.copy(serviceUpdateNotify = false)
                    onValueChange(updatedAlarm)
                }
            }
        )
    }
}

@Composable
private fun EventAlarmOffDialog(
    onUserClick: (Boolean) -> Unit,
) {
    DialogComponent.DefaultButtonTwo(
        title = stringResource(R.string.alarm_view_dialog_title),
        description = stringResource(R.string.alarm_view_dialog_subtitle),
        buttonTextStart = stringResource(R.string.alarm_view_dialog_start_btn),
        buttonTextEnd = stringResource(R.string.alarm_view_dialog_end_btn),
        onClick = {
            onUserClick(true)
        },
        onDismiss = {
            onUserClick(false)
        },
        startButtonTextColor = NeutralColor.GRAY_600
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "Service Alarm View")
@Composable
private fun ServiceAlarmViewPreview() {
    Surface {
        ServiceAlarmView(
            value = Alarm(
                commentCardNotify = true,
                cardLikeNotify = false,
                followUserCardNotify = true,
                newFollowerNotify = false,
                cardNewCommentNotify = true,
                recommendedContentNotify = true,
                favoriteTagNotify = false,
                policyViolationNotify = true,
                serviceUpdateNotify = true
            ),
            onValueChange = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "Event Alarm View")
@Composable
private fun EventAlarmPreview() {
    Surface {
        EventAlarm(
            value = Alarm(
                commentCardNotify = false,
                cardLikeNotify = false,
                followUserCardNotify = false,
                newFollowerNotify = false,
                cardNewCommentNotify = false,
                recommendedContentNotify = false,
                favoriteTagNotify = false,
                policyViolationNotify = false,
                serviceUpdateNotify = true
            ),
            onValueChange = {}
        )
    }
}