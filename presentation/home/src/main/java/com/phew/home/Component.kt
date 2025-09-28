package com.phew.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.phew.core_design.NeutralColor
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import com.phew.core_design.TextComponent
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.phew.core_design.Primary
import com.phew.domain.dto.FeedLikeNotification
import com.phew.domain.dto.FollowNotification
import com.phew.domain.dto.Notice
import com.phew.domain.dto.Notification
import com.phew.domain.dto.Notify
import com.phew.domain.dto.UserBlockNotification
import com.phew.domain.dto.UserCommentLike
import com.phew.domain.dto.UserCommentWrite
import com.phew.domain.dto.UserDeleteNotification

@Composable
internal fun AnimatedNoticeTabLayout(
    selectTabData: Int,
    allClick: () -> Unit,
    noticeClick: () -> Unit,
    isTabsVisible: Boolean
) {
    val tabItem = listOf(
        stringResource(R.string.home_notice_activate),
        stringResource(R.string.home_notice_notice)
    )
    AnimatedVisibility(
        visible = isTabsVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(durationMillis = 150)
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(durationMillis = 150)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(color = NeutralColor.WHITE)
        ) {
            TabRow(
                selectedTabIndex = selectTabData,
                modifier = Modifier
                    .wrapContentWidth(align = Alignment.Start)
                    .height(56.dp)
                    .padding(start = 16.dp, end = 16.dp),
                containerColor = NeutralColor.WHITE,
                contentColor = NeutralColor.BLACK,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectTabData]),
                        height = 2.dp,
                        color = NeutralColor.BLACK
                    )
                },
                divider = {}
            ) {
                tabItem.forEachIndexed { index, title ->
                    val isSelected = selectTabData == index
                    Tab(
                        selected = isSelected,
                        onClick = {
                            when (index) {
                                NAV_NOTICE_ACTIVATE -> allClick()
                                NAV_NOTICE_NOTIFY_INDEX -> noticeClick()
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                style = TextComponent.BODY_1_M_14,
                                color = if (isSelected) NeutralColor.GRAY_600 else NeutralColor.GRAY_400,
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun AnimatedFeedTabLayout(
    selectTabData: Int,
    recentClick: () -> Unit,
    popularClick: () -> Unit,
    nearClick: () -> Unit,
    isTabsVisible: Boolean,
    onDistanceClick: (Int) -> Unit
) {
    val tabItem = listOf(
        stringResource(R.string.home_feed_tab_recent_card),
        stringResource(R.string.home_feed_tab_popular_card),
        stringResource(R.string.home_feed_tab_near_card)
    )

    AnimatedVisibility(
        visible = isTabsVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(durationMillis = 150)
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(durationMillis = 150)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(color = NeutralColor.WHITE)
        ) {
            TabRow(
                selectedTabIndex = selectTabData,
                modifier = Modifier
                    .wrapContentWidth(align = Alignment.Start)
                    .height(56.dp)
                    .padding(start = 16.dp, end = 16.dp),
                containerColor = NeutralColor.WHITE,
                contentColor = NeutralColor.BLACK,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectTabData]),
                        height = 2.dp,
                        color = NeutralColor.BLACK
                    )
                },
                divider = {}
            ) {
                tabItem.forEachIndexed { index, title ->
                    val isSelected = selectTabData == index
                    Tab(
                        selected = isSelected,
                        onClick = {
                            when (index) {
                                NAV_HOME_FEED_INDEX -> recentClick()
                                NAV_HOME_POPULAR_INDEX -> popularClick()
                                NAV_HOME_NEAR_INDEX -> nearClick()
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                style = TextComponent.TITLE_2_SB_16,
                                color = if (isSelected) NeutralColor.BLACK else NeutralColor.GRAY_400
                            )
                        },
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp),
                color = NeutralColor.GRAY_200
            )
            if (selectTabData == NAV_HOME_NEAR_INDEX) {
                var selectDistance by remember { mutableIntStateOf(DISTANCE_1KM) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(color = NeutralColor.WHITE)
                        .padding(start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DistanceText(
                        distance = stringResource(R.string.home_feed_1km_distance),
                        onClick = { onDistanceClick(DISTANCE_1KM) },
                        isSelect = selectDistance == DISTANCE_1KM
                    )
                    DistanceText(
                        distance = stringResource(R.string.home_feed_5km_distance),
                        onClick = { onDistanceClick(DISTANCE_5KM) },
                        isSelect = selectDistance == DISTANCE_5KM
                    )
                    DistanceText(
                        distance = stringResource(R.string.home_feed_10km_distance),
                        onClick = { onDistanceClick(DISTANCE_10KM) },
                        isSelect = selectDistance == DISTANCE_10KM
                    )
                    DistanceText(
                        distance = stringResource(R.string.home_feed_20km_distance),
                        onClick = { onDistanceClick(DISTANCE_20KM) },
                        isSelect = selectDistance == DISTANCE_20KM
                    )
                }
            }
        }
    }
}

@Composable
private fun DistanceText(distance: String, onClick: (String) -> Unit, isSelect: Boolean) {
    Text(
        text = distance,
        style = TextComponent.SUBTITLE_3_SB_14,
        color = if (isSelect) NeutralColor.BLACK else NeutralColor.GRAY_400,
        modifier = Modifier
            .width(48.dp)
            .height(37.dp)
            .padding(start = 10.dp, top = 8.dp, end = 10.dp, bottom = 8.dp)
            .clickable { onClick(distance) }
    )
}

@Composable
internal fun NoticeComponentView(data: Notice) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(182.dp)
            .background(color = NeutralColor.WHITE)
            .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(com.phew.core_design.R.drawable.ic_notification),
                contentDescription = data.title,
            )
            Text(
                text = stringResource(R.string.home_notice_notice),
                style = TextComponent.CAPTION_1_SB_12,
                color = NeutralColor.GRAY_400,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
            Text(
                text = data.viewTime,
                style = TextComponent.CAPTION_1_SB_12,
                color = NeutralColor.GRAY_400
            )
        }
        Text(
            text = data.title,
            style = TextComponent.SUBTITLE_1_M_16,
            color = NeutralColor.GRAY_600,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun NotifyViewUnread(data: Notification) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(102.dp)
            .background(color = Primary.LIGHT_1)
            .padding(horizontal = 24.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = when (data) {
                    is FollowNotification -> painterResource(com.phew.core_design.R.drawable.ic_users_filled)
                    is UserBlockNotification,
                    is UserDeleteNotification -> painterResource(com.phew.core_design.R.drawable.ic_danger)
                    else -> painterResource(com.phew.core_design.R.drawable.ic_card_filled_blue)
                },
                contentDescription = ""
            )
            Text(
                text = when (data) {
                    is FollowNotification,
                    is UserCommentLike,
                    is UserCommentWrite -> stringResource(R.string.home_notice_item_follow)
                    is UserBlockNotification,
                    is UserDeleteNotification -> stringResource(R.string.home_notice_item_limit)
                    is FeedLikeNotification -> stringResource(R.string.home_notice_item_feed_like)
                },
                style = TextComponent.CAPTION_1_SB_12,
                color = NeutralColor.GRAY_400,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
            Text(
                text = when(data){
                    is FeedLikeNotification -> data.viewTime
                    is FollowNotification -> data.viewTime
                    is UserBlockNotification -> data.viewTime
                    is UserCommentLike -> data.viewTime
                    is UserCommentWrite -> data.viewTime
                    is UserDeleteNotification -> data.viewTime
                },
                style = TextComponent.CAPTION_1_SB_12,
                color = NeutralColor.GRAY_400
            )
        }
        Text(
            text = when(data){
                is FeedLikeNotification -> stringResource(R.string.home_notice_like_comment , data.nickName)
                is FollowNotification -> stringResource(R.string.home_notice_follow_comment , data.nickName)
                is UserBlockNotification -> stringResource(R.string.home_notice_limit_card_comment , data.blockTimeView)
                is UserCommentLike -> stringResource(R.string.home_notice_limit_card_comment , data.nickName)
                is UserCommentWrite -> stringResource(R.string.home_notice_under_card_comment , data.nickName)
                is UserDeleteNotification -> stringResource(R.string.home_notice_delete_card)
            },
            style = TextComponent.TITLE_2_SB_16,
            color = NeutralColor.GRAY_600,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
    }
}

@Composable
internal fun NotifyViewReadd(data: Notify) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(102.dp)
            .background(color = NeutralColor.WHITE)
            .padding(horizontal = 24.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = when (data.category) {
                    NOTIFY_LIMIT -> painterResource(com.phew.core_design.R.drawable.ic_danger)
                    NOTIFY_FOLLOW -> painterResource(com.phew.core_design.R.drawable.ic_users_filled)
                    else -> painterResource(com.phew.core_design.R.drawable.ic_check)
                },
                contentDescription = data.category
            )
            Text(
                text = data.title,
                style = TextComponent.CAPTION_1_SB_12,
                color = NeutralColor.GRAY_400,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
            Text(
                text = data.time,
                style = TextComponent.CAPTION_1_SB_12,
                color = NeutralColor.GRAY_400
            )
        }
        Text(
            text = data.content,
            style = TextComponent.TITLE_2_SB_16,
            color = NeutralColor.GRAY_600,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
    }
}
