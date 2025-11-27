package com.phew.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phew.core_common.TimeUtils
import com.phew.core_design.NeutralColor
import com.phew.core_design.NeutralColor.GRAY_400
import com.phew.core_design.NeutralColor.GRAY_600
import com.phew.core_design.NeutralColor.WHITE
import com.phew.core_design.Primary
import com.phew.core_design.TextComponent
import com.phew.core_design.UnKnowColor
import com.phew.core_design.component.card.FeedAdminCard
import com.phew.core_design.component.card.FeedDefaultCard
import com.phew.core_design.component.card.FeedDeletedCard
import com.phew.core_design.component.card.FeedPungCard
import com.phew.core_design.component.card.NotiCard
import com.phew.core_design.component.card.NotiCardData
import com.phew.core_design.component.card.component.IndicatorDot
import com.phew.core_design.component.tab.SooumTab
import com.phew.core_design.component.tab.SooumTabRow
import com.phew.core_design.label.LabelComponent
import com.phew.core_design.theme.MAIN
import com.phew.core_design.theme.M_YELLOW
import com.phew.domain.dto.FeedCardType
import com.phew.domain.dto.FeedLikeNotification
import com.phew.domain.dto.FollowNotification
import com.phew.domain.dto.Notice
import com.phew.domain.dto.Notification
import com.phew.domain.dto.UserBlockNotification
import com.phew.domain.dto.UserCommentLike
import com.phew.domain.dto.UserCommentWrite
import com.phew.domain.dto.UserDeleteNotification
import com.phew.feed.FeedUi.TypedFeedCardView
import com.phew.feed.viewModel.DistanceType
import com.phew.presentation.feed.R
import kotlinx.coroutines.delay

object FeedUi {

    @Composable
    internal fun FeedNoticeView(
        feedNotice: List<Notice>,
        feedNoticeClick: (String) -> Unit,
    ) {
        if (feedNotice.isEmpty()) return
        val pagerState = rememberPagerState(
            initialPage = Int.MAX_VALUE / 2 - ((Int.MAX_VALUE / 2) % feedNotice.size),
            pageCount = { Int.MAX_VALUE }
        )
        LaunchedEffect(Unit) {
            while (true) {
                delay(5000L)
                val nextPage = pagerState.currentPage + 1
                pagerState.animateScrollToPage(nextPage)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(71.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) { page ->
                val actualIndex = page % feedNotice.size
                val currentNotice = feedNotice[actualIndex]
                val cardData = NotiCardData(
                    title = when (currentNotice.type) {
                        Notice.NoticeType.ANNOUNCEMENT -> stringResource(R.string.home_notice_notice)
                        Notice.NoticeType.NEWS -> stringResource(R.string.home_notice_news)
                        Notice.NoticeType.MAINTENANCE -> stringResource(R.string.home_notice_service)
                    },
                    description = currentNotice.content,
                    id = currentNotice.id.toString(),
                    iconRes = when (currentNotice.type) {
                        Notice.NoticeType.ANNOUNCEMENT -> com.phew.core_design.R.drawable.ic_tool_filled
                        Notice.NoticeType.NEWS -> com.phew.core_design.R.drawable.ic_mail_filled_bule
                        Notice.NoticeType.MAINTENANCE -> com.phew.core_design.R.drawable.ic_headset_filled_yellow
                    },
                    iconTint = when (currentNotice.type) {
                        Notice.NoticeType.ANNOUNCEMENT -> GRAY_400
                        Notice.NoticeType.NEWS -> MAIN
                        Notice.NoticeType.MAINTENANCE -> M_YELLOW
                    },
                    iconBackgroundColor = NeutralColor.GRAY_100,
                )
                NotiCard(
                    data = cardData,
                    onClick = { feedNoticeClick(currentNotice.url) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 16.dp,
                            spotColor = UnKnowColor.color,
                            ambientColor = UnKnowColor.color
                        )
                )
            }
            IndicatorDot(
                pagerState = pagerState,
                totalSize = feedNotice.size,
                modifier = Modifier
                    .padding(top = 16.dp, end = 16.dp)
                    .align(Alignment.TopEnd)
            )
        }
    }

    @Composable
    internal fun AnimatedFeedTabLayout(
        selectTabData: Int,
        recentClick: () -> Unit,
        popularClick: () -> Unit,
        nearClick: () -> Unit,
        isTabsVisible: Boolean,
        onDistanceClick: (DistanceType) -> Unit,
        selectDistanceType: DistanceType,
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
                    .background(color = WHITE)
            ) {
                SooumTabRow(
                    selectedTabIndex = selectTabData,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    tabItem.forEachIndexed { index, title ->
                        SooumTab(
                            selected = selectTabData == index,
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
                                    color = LocalContentColor.current
                                )
                            }
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(color = WHITE)
                            .padding(start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DistanceText(
                            distance = stringResource(R.string.home_feed_1km_distance),
                            onClick = { onDistanceClick(DistanceType.KM_1) },
                            isSelect = selectDistanceType == DistanceType.KM_1
                        )
                        DistanceText(
                            distance = stringResource(R.string.home_feed_5km_distance),
                            onClick = { onDistanceClick(DistanceType.KM_5) },
                            isSelect = selectDistanceType == DistanceType.KM_5
                        )
                        DistanceText(
                            distance = stringResource(R.string.home_feed_10km_distance),
                            onClick = { onDistanceClick(DistanceType.KM_10) },
                            isSelect = selectDistanceType == DistanceType.KM_10
                        )
                        DistanceText(
                            distance = stringResource(R.string.home_feed_20km_distance),
                            onClick = { onDistanceClick(DistanceType.KM_20) },
                            isSelect = selectDistanceType == DistanceType.KM_20
                        )
                        DistanceText(
                            distance = stringResource(R.string.home_feed_50km_distance),
                            onClick = { onDistanceClick(DistanceType.KM_50) },
                            isSelect = selectDistanceType == DistanceType.KM_50
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun DistanceText(distance: String, onClick: () -> Unit, isSelect: Boolean) {
        Text(
            text = distance,
            style = TextComponent.SUBTITLE_3_SB_14,
            color = if (isSelect) NeutralColor.BLACK else GRAY_400,
            modifier = Modifier
                .wrapContentWidth()
                .height(37.dp)
                .padding(start = 10.dp, top = 8.dp, end = 10.dp, bottom = 8.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onClick() }
                )
        )
    }


    @Composable
    internal fun TypedFeedCardView(
        feedCard: FeedCardType,
        onClick: (String) -> Unit,
        onRemoveCard: (String) -> Unit,
    ) {
        when (feedCard) {
            is FeedCardType.BoombType -> PungTypeCard(
                feedCard = feedCard,
                onClick = onClick,
                onRemoveCard = onRemoveCard
            )

            is FeedCardType.AdminType -> AdminTypeCard(
                feedCard = feedCard,
                onClick = onClick
            )

            is FeedCardType.NormalType -> NormalTypeCard(
                feedCard = feedCard,
                onClick = onClick
            )
        }
    }

    @Composable
    internal fun PungTypeCard(
        feedCard: FeedCardType.BoombType,
        onClick: (String) -> Unit,
        onRemoveCard: (String) -> Unit,
    ) {
        var remainingTimeMillis by remember {
            mutableLongStateOf(TimeUtils.remainingMillisUntil(feedCard.storyExpirationTime))
        }
        var isExpired by remember { mutableStateOf(false) }

        // 타이머 실행
        LaunchedEffect(feedCard.cardId) {
            while (remainingTimeMillis > 0) {
                delay(1000L)
                remainingTimeMillis -= 1000L
            }
            isExpired = true
        }
        if (isExpired) {
            FeedDeletedCard(
                id = feedCard.cardId,
                onClick = {
                    onRemoveCard(feedCard.cardId)
                }
            )
        } else {
            FeedPungCard(
                id = feedCard.cardId,
                imgUrl = feedCard.imageUrl,
                contentText = feedCard.content,
                font = feedCard.font,
                distance = feedCard.location ?: "",
                timeAgo = feedCard.writeTime,
                commentCount = feedCard.commentValue,
                likeCount = feedCard.likeValue,
                remainingTimeMillis = remainingTimeMillis,
                onClick = {
                    onClick(feedCard.cardId)
                }
            )
        }
    }

    @Composable
    internal fun AdminTypeCard(
        feedCard: FeedCardType.AdminType,
        onClick: (String) -> Unit,
    ) {
        FeedAdminCard(
            id = feedCard.cardId,
            imgUrl = feedCard.imageUrl,
            contentText = feedCard.content,
            font = feedCard.font,
            timeAgo = feedCard.writeTime,
            commentCount = feedCard.commentValue,
            likeCount = feedCard.likeValue,
            onClick = {
                onClick(feedCard.cardId)
            }
        )
    }

    @Composable
    internal fun NormalTypeCard(
        feedCard: FeedCardType.NormalType,
        onClick: (String) -> Unit,
    ) {
        FeedDefaultCard(
            id = feedCard.cardId,
            imgUrl = feedCard.imageUrl,
            contentText = feedCard.content,
            font = feedCard.font,
            distance = feedCard.location ?: "",
            timeAgo = feedCard.writeTime,
            commentCount = feedCard.commentValue,
            likeCount = feedCard.likeValue,
            onClick = {
                onClick(feedCard.cardId)
            }
        )
    }
}

object NotificationUi {

    @Composable
    internal fun NotifyTabBar(
        selectData: NotifyTab,
        onClick: (NotifyTab) -> Unit,
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            LabelComponent.LabelView(
                text = stringResource(R.string.home_notice_activate),
                textColor = if (selectData == NotifyTab.NOTIFY_ACTIVATE) GRAY_600 else GRAY_400,
                backgroundColor = if (selectData == NotifyTab.NOTIFY_ACTIVATE) GRAY_600 else WHITE,
                onClick = { onClick(NotifyTab.NOTIFY_ACTIVATE) }
            )
            LabelComponent.LabelView(
                text = stringResource(R.string.home_notice_notice),
                textColor = if (selectData == NotifyTab.NOTIFY_SERVICE) GRAY_600 else GRAY_400,
                backgroundColor = if (selectData == NotifyTab.NOTIFY_SERVICE) GRAY_600 else WHITE,
                onClick = { onClick(NotifyTab.NOTIFY_SERVICE) }
            )
        }
    }


    @Composable
    internal fun NoticeComponentView(data: Notice) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(102.dp)
                .background(color = WHITE)
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
                    painter = painterResource(
                        id = when (data.type) {
                            Notice.NoticeType.ANNOUNCEMENT -> com.phew.core_design.R.drawable.ic_tool_filled
                            Notice.NoticeType.NEWS -> com.phew.core_design.R.drawable.ic_mail_filled_bule
                            Notice.NoticeType.MAINTENANCE -> com.phew.core_design.R.drawable.ic_headset_filled_yellow
                        }
                    ),
                    contentDescription = data.content,
                    colorFilter = ColorFilter.tint(when(data.type){
                        Notice.NoticeType.ANNOUNCEMENT -> GRAY_400
                        Notice.NoticeType.NEWS -> MAIN
                        Notice.NoticeType.MAINTENANCE -> M_YELLOW
                    })
                )
                Text(
                    text = stringResource(R.string.home_notice_notice),
                    style = TextComponent.CAPTION_1_SB_12,
                    color = GRAY_400,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )
                Text(
                    text = data.viewTime,
                    style = TextComponent.CAPTION_1_SB_12,
                    color = GRAY_400
                )
            }
            Text(
                text = data.content,
                style = TextComponent.SUBTITLE_1_M_16,
                color = GRAY_600,
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
                        is UserDeleteNotification,
                            -> painterResource(com.phew.core_design.R.drawable.ic_danger)

                        else -> painterResource(com.phew.core_design.R.drawable.ic_card_filled_blue)
                    },
                    contentDescription = ""
                )
                Text(
                    text = when (data) {
                        is FollowNotification,
                        is UserCommentLike,
                        is UserCommentWrite,
                            -> stringResource(R.string.home_notice_item_follow)

                        is UserBlockNotification,
                        is UserDeleteNotification,
                            -> stringResource(R.string.home_notice_item_limit)

                        is FeedLikeNotification -> stringResource(R.string.home_notice_item_feed_like)
                    },
                    style = TextComponent.CAPTION_1_SB_12,
                    color = GRAY_400,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )
                Text(
                    text = when (data) {
                        is FeedLikeNotification -> data.viewTime
                        is FollowNotification -> data.viewTime
                        is UserBlockNotification -> data.viewTime
                        is UserCommentLike -> data.viewTime
                        is UserCommentWrite -> data.viewTime
                        is UserDeleteNotification -> data.viewTime
                    },
                    style = TextComponent.CAPTION_1_SB_12,
                    color = GRAY_400
                )
            }
            Text(
                text = when (data) {
                    is FeedLikeNotification -> stringResource(
                        R.string.home_notice_like_comment,
                        data.nickName
                    )

                    is FollowNotification -> stringResource(
                        R.string.home_notice_follow_comment,
                        data.nickName
                    )

                    is UserBlockNotification -> stringResource(
                        R.string.home_notice_limit_card_comment,
                        data.blockTimeView
                    )

                    is UserCommentLike -> stringResource(
                        R.string.home_notice_limit_card_comment,
                        data.nickName
                    )

                    is UserCommentWrite -> stringResource(
                        R.string.home_notice_under_card_comment,
                        data.nickName
                    )

                    is UserDeleteNotification -> stringResource(R.string.home_notice_delete_card)
                },
                style = TextComponent.TITLE_2_SB_16,
                color = GRAY_600,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }
    }

    @Composable
    internal fun NotifyViewRead(data: Notification) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(102.dp)
                .background(color = WHITE)
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
                        is UserDeleteNotification,
                            -> painterResource(com.phew.core_design.R.drawable.ic_danger)

                        else -> painterResource(com.phew.core_design.R.drawable.ic_card_filled_blue)
                    },
                    contentDescription = ""
                )
                Text(
                    text = when (data) {
                        is FollowNotification,
                        is UserCommentLike,
                        is UserCommentWrite,
                            -> stringResource(R.string.home_notice_item_follow)

                        is UserBlockNotification,
                        is UserDeleteNotification,
                            -> stringResource(R.string.home_notice_item_limit)

                        is FeedLikeNotification -> stringResource(R.string.home_notice_item_feed_like)
                    },
                    style = TextComponent.CAPTION_1_SB_12,
                    color = GRAY_400,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )
                Text(
                    text = when (data) {
                        is FeedLikeNotification -> data.viewTime
                        is FollowNotification -> data.viewTime
                        is UserBlockNotification -> data.viewTime
                        is UserCommentLike -> data.viewTime
                        is UserCommentWrite -> data.viewTime
                        is UserDeleteNotification -> data.viewTime
                    },
                    style = TextComponent.CAPTION_1_SB_12,
                    color = GRAY_400
                )
            }
            Text(
                text = when (data) {
                    is FeedLikeNotification -> stringResource(
                        R.string.home_notice_like_comment,
                        data.nickName
                    )

                    is FollowNotification -> stringResource(
                        R.string.home_notice_follow_comment,
                        data.nickName
                    )

                    is UserBlockNotification -> stringResource(
                        R.string.home_notice_limit_card_comment,
                        data.blockTimeView
                    )

                    is UserCommentLike -> stringResource(
                        R.string.home_notice_limit_card_comment,
                        data.nickName
                    )

                    is UserCommentWrite -> stringResource(
                        R.string.home_notice_under_card_comment,
                        data.nickName
                    )

                    is UserDeleteNotification -> stringResource(R.string.home_notice_delete_card)
                },
                style = TextComponent.TITLE_2_SB_16,
                color = GRAY_600,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }
    }
}

@Preview
@Composable
private fun BoombTypeCardPreview() {
    val sampleBoombCard = FeedCardType.BoombType(
        cardId = "boom_preview_1",
        storyExpirationTime = "01:30:45",
        content = "🔥 30분 후 사라지는 피드입니다!\n지금 확인하세요",
        imageUrl = "",
        imageName = "",
        font = "bold",
        location = "150m",
        writeTime = "2025-01-15T10:30:00",
        commentValue = "12",
        likeValue = "45"
    )

    TypedFeedCardView(
        feedCard = sampleBoombCard,
        onClick = { },
        onRemoveCard = { }
    )
}

@Preview
@Composable
private fun AdminTypeCardPreview() {
    val sampleAdminCard = FeedCardType.AdminType(
        cardId = "admin_preview_1",
        content = "📢 [관리자 공지] 앱 업데이트 안내\n새로운 기능이 추가되었습니다",
        imageUrl = "",
        imageName = "",
        font = "bold",
        location = "100m",
        writeTime = "2025-01-15T09:00:00",
        commentValue = "25",
        likeValue = "78"
    )

    TypedFeedCardView(feedCard = sampleAdminCard, onClick = {}, onRemoveCard = {})
}

@Preview
@Composable
private fun NormalTypeCardPreview() {
    val sampleNormalCard = FeedCardType.NormalType(
        cardId = "normal_preview_1",
        content = "오늘 날씨가 정말 좋네요! ☀️\n산책하기 딱 좋은 날씨입니다",
        imageUrl = "",
        imageName = "",
        font = "medium",
        location = "100m",
        writeTime = "2025-01-15T11:00:00",
        commentValue = "8",
        likeValue = "23"
    )

    TypedFeedCardView(feedCard = sampleNormalCard, onClick = {}, onRemoveCard = {})
}

private const val TAG = "Feed Component"
