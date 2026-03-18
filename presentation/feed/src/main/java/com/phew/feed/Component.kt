package com.phew.feed

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.view.isVisible
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.phew.core_common.TimeUtils
import com.phew.core_design.Danger
import com.phew.core_design.NeutralColor
import com.phew.core_design.NeutralColor.GRAY_400
import com.phew.core_design.NeutralColor.GRAY_600
import com.phew.core_design.NeutralColor.WHITE
import com.phew.core_design.Primary
import com.phew.core_design.TextComponent
import com.phew.core_design.component.card.FeedAdminCard
import com.phew.core_design.component.card.FeedDefaultCard
import com.phew.core_design.component.card.FeedDeletedCard
import com.phew.core_design.component.card.FeedPungCard
import com.phew.core_design.component.card.NoticeCardData
import com.phew.core_design.component.card.FeedNotice
import com.phew.core_design.component.filter.SooumFilter
import com.phew.core_design.component.tab.SooumTab
import com.phew.core_design.component.tab.SooumTabRow
import com.phew.core_design.label.LabelComponent
import com.phew.core_design.theme.GRAY_100
import com.phew.core_design.theme.GRAY_500
import com.phew.core_design.theme.MAIN
import com.phew.core_design.theme.M_YELLOW
import com.phew.core_design.theme.unknownColor
import com.phew.domain.dto.CardArticle
import com.phew.domain.dto.FeedCardType
import com.phew.domain.dto.FeedLikeNotification
import com.phew.domain.dto.FollowNotification
import com.phew.domain.dto.Notice
import com.phew.domain.dto.Notification
import com.phew.domain.dto.UserBlockNotification
import com.phew.domain.dto.UserCommentLike
import com.phew.domain.dto.UserCommentWrite
import com.phew.domain.dto.UserDeleteNotification
import com.phew.domain.dto.UserTagNotification
import com.phew.feed.FeedUi.TypedFeedCardView
import com.phew.feed.viewModel.DistanceType
import com.phew.presentation.feed.R
import com.phew.presentation.feed.databinding.ItemNativeAdBinding
import kotlinx.coroutines.delay
import com.phew.core_design.R as DesignR

object FeedUi {
    @Composable
    fun NativeAdLoaderScreen(adUnitId: String) {


        AndroidViewBinding(
            factory = ItemNativeAdBinding::inflate
        ) {
            val adView = root.also { adView ->
                adView.bodyView = this.adBody
                adView.callToActionView = this.adCallToAction
                adView.headlineView = this.adHeadline
                adView.iconView = this.adAppIcon
            }

            val adContainer = this.adContainer

            val adLoader = AdLoader.Builder(adView.context, adUnitId)
                .forNativeAd { nativeAd ->
                    nativeAd.advertiser?.let {

                    }
                    nativeAd.body?.let { body ->
                        this.adBody.text = body
                    }

                    nativeAd.headline?.let {
                        this.adHeadline.text = it
                    }
                    nativeAd.icon?.let {
                        this.adAppIcon.setImageDrawable(it.drawable)
                    }
                    adView.setNativeAd(nativeAd)
                }.withAdListener(object : AdListener() {
                    override fun onAdLoaded() {
                        Log.i("Admob", "onAdLoaded : Native ad Loaded")
                        adContainer.isVisible = true
                        super.onAdLoaded()
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.e("AdMob", "onAdFailedToLoad : ${error.message}")
                        super.onAdFailedToLoad(error)
                    }
                }).withNativeAdOptions(
                    NativeAdOptions.Builder().setAdChoicesPlacement(
                        NativeAdOptions.ADCHOICES_TOP_RIGHT
                    ).build()
                ).build()
            adContainer.isVisible = true
            adLoader.loadAd(AdRequest.Builder().build())
        }
    }

    @Composable
     fun CardArticleView(
        data: CardArticle,
        modifier: Modifier,
        onCardClick: (cardId: Long) -> Unit
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(83.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = unknownColor,
                    ambientColor = unknownColor
                )
                .background(color = WHITE, shape = RoundedCornerShape(16.dp))
                .border(width = 1.dp, color = GRAY_100, shape = RoundedCornerShape(16.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onCardClick(data.cardId) }
                )
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CardArticleProfileImage(
                profileImage = data.profileImgUrl,
                isRead = false,
                description = data.cardContent
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.nickName,
                    style = TextComponent.CAPTION_2_M_12,
                    color = GRAY_400
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = data.cardContent.replace("\n", " ")
                        .let { if (it.length > 17) "${it.take(17)}..." else it },
                    style = TextComponent.SUBTITLE_3_SB_14,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = GRAY_600
                )
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy((-6).dp, Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        data.writerProfileImageUrls.forEach { imageUrl ->
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageUrl.ifEmpty { DesignR.drawable.ic_profile })
                                    .crossfade(true)
                                    .build(),
                                contentDescription = data.cardContent,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 1.dp,
                                        color = WHITE,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(1.dp))
                    Text(
                        text = if (data.totalWriterCnt == 0) {
                            stringResource(id = R.string.home_article_write_first)
                        } else {
                            stringResource(
                                id = R.string.home_article_write,
                                data.totalWriterCnt
                            )
                        },
                        style = TextComponent.CAPTION_2_M_12,
                        color = GRAY_500
                    )
                }
            }
        }
    }

    @Composable
    private fun CardArticleProfileImage(
        profileImage: String,
        isRead: Boolean,
        description: String,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .padding(1.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(profileImage.ifEmpty { DesignR.drawable.ic_profile })
                    .crossfade(true).build(),
                contentDescription = description,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
            )
            if (!isRead) {
                Box(
                    modifier = Modifier
                        .padding(1.dp)
                        .size(10.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-1).dp, y = (-2).dp)
                        .background(color = Danger.M_RED, shape = CircleShape)
                        .border(width = 2.dp, color = WHITE, shape = CircleShape)
                )
            }
        }
    }

    @Composable
    internal fun FeedNoticeView(
        noticeList: List<Notice>,
        feedNoticeClick: (url : String) -> Unit,
        deleteNotice : (id : Int) -> Unit,
        modifier: Modifier = Modifier
    ) {
        val currentNotice = noticeList.firstOrNull()
        AnimatedContent(
            targetState = currentNotice,
            transitionSpec = {
                val enter = scaleIn(
                    animationSpec = tween(300, delayMillis = 300),
                    initialScale = 0.9f
                ) + fadeIn(animationSpec = tween(300, delayMillis = 300))

                val exit = scaleOut(
                    animationSpec = tween(300),
                    targetScale = 0.9f
                ) + fadeOut(animationSpec = tween(300))

                (enter togetherWith exit).apply {
                    targetContentZIndex = -1f
                }
            },
            contentKey = { it?.id ?: "empty_notice" },
            label = "NoticeTransitionAnimation",
            modifier = modifier
        ) { notice ->
            if (notice != null) {
                FeedNotice(
                    data = NoticeCardData(
                        id = notice.id.toString(),
                        description = notice.content,
                        iconRes = when (notice.type) {
                            Notice.NoticeType.ANNOUNCEMENT -> DesignR.drawable.ic_headset_filled_yellow
                            Notice.NoticeType.NEWS -> DesignR.drawable.ic_mail_filled_bule
                            Notice.NoticeType.MAINTENANCE -> DesignR.drawable.ic_tool_filled
                        },
                        iconTint = when (notice.type) {
                            Notice.NoticeType.ANNOUNCEMENT -> M_YELLOW
                            Notice.NoticeType.NEWS -> MAIN
                            Notice.NoticeType.MAINTENANCE -> GRAY_400
                        },
                        iconBackgroundColor = NeutralColor.GRAY_100,
                    ),
                    onClick = { feedNoticeClick(notice.url) },
                    onCloseClick = { noticeId ->
                        deleteNotice(noticeId)
                    },
                    modifier = Modifier
                )
            } else {
                Spacer(modifier = Modifier.height(0.dp))
            }
        }
    }

    @Composable
    internal fun FeedTab(
        selectTabData: Int,
        recentClick: () -> Unit,
        popularClick: () -> Unit,
        nearClick: () -> Unit,
        onDistanceClick: (DistanceType) -> Unit,
        selectDistanceType: DistanceType,
    ) {
        val tabItem = listOf(
            stringResource(R.string.home_feed_tab_recent_card),
            stringResource(R.string.home_feed_tab_popular_card),
            stringResource(R.string.home_feed_tab_near_card)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = WHITE)
        ) {
            SooumTabRow(
                selectedTabIndex = selectTabData,
                modifier = Modifier.fillMaxWidth()
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
            if (selectTabData == NAV_HOME_NEAR_INDEX) {
                SooumFilter(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp),
                    selectedFilter = selectDistanceType,
                    filters = listOf(DistanceType.KM_1, DistanceType.KM_5, DistanceType.KM_10, DistanceType.KM_20, DistanceType.KM_50),
                    onFilterSelected = onDistanceClick,
                    labelProvider = { distanceType ->
                        when (distanceType) {
                            DistanceType.KM_1 -> stringResource(R.string.home_feed_1km_distance)
                            DistanceType.KM_5 -> stringResource(R.string.home_feed_5km_distance)
                            DistanceType.KM_10 -> stringResource(R.string.home_feed_10km_distance)
                            DistanceType.KM_20 -> stringResource(R.string.home_feed_20km_distance)
                            DistanceType.KM_50 -> stringResource(R.string.home_feed_50km_distance)
                        }
                    }
                )
            }
        }
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    bottom = 9.5.dp, start = 16.dp,
                    end = 16.dp
                ), horizontalArrangement = Arrangement.Start
        ) {
            LabelComponent.LabelView(
                text = stringResource(R.string.home_notice_activate),
                textColor = if (selectData == NotifyTab.NOTIFY_ACTIVATE) GRAY_600 else GRAY_400,
                backgroundColor = if (selectData == NotifyTab.NOTIFY_ACTIVATE) GRAY_100 else WHITE,
                onClick = { onClick(NotifyTab.NOTIFY_ACTIVATE) }
            )
            LabelComponent.LabelView(
                text = stringResource(R.string.home_notice_notice),
                textColor = if (selectData == NotifyTab.NOTIFY_SERVICE) GRAY_600 else GRAY_400,
                backgroundColor = if (selectData == NotifyTab.NOTIFY_SERVICE) GRAY_100 else WHITE,
                onClick = { onClick(NotifyTab.NOTIFY_SERVICE) }
            )
        }
    }


    @Composable
    internal fun NoticeComponentView(data: Notice, onClick: (String) -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .background(color = WHITE)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        onClick(data.url)
                    }
                )
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
                            Notice.NoticeType.ANNOUNCEMENT -> DesignR.drawable.ic_tool_filled
                            Notice.NoticeType.NEWS -> DesignR.drawable.ic_mail_filled_bule
                            Notice.NoticeType.MAINTENANCE -> DesignR.drawable.ic_headset_filled_yellow
                        }
                    ),
                    contentDescription = data.content,
                    colorFilter = ColorFilter.tint(
                        when (data.type) {
                            Notice.NoticeType.ANNOUNCEMENT -> GRAY_400
                            Notice.NoticeType.NEWS -> MAIN
                            Notice.NoticeType.MAINTENANCE -> M_YELLOW
                        }
                    )
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp)
            )
        }
    }

    @Composable
    internal fun NotifyViewUnread(
        data: Notification,
        onItemExpose: (Long) -> Unit,
        onCardClick: (cardId: Long) -> Unit,
    ) {
        LaunchedEffect(data.notificationId) {
            onItemExpose(data.notificationId)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(102.dp)
                .background(color = Primary.LIGHT_1)
                .padding(horizontal = 24.dp, vertical = 18.dp)
                .then(
                    if (data is UserCommentWrite) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                onCardClick(data.targetCardId)
                            }
                        )
                    } else {
                        Modifier
                    }
                )
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
                        is FollowNotification -> painterResource(DesignR.drawable.ic_users_filled)
                        is UserBlockNotification,
                        is UserDeleteNotification,
                            -> painterResource(DesignR.drawable.ic_danger)
                        is UserTagNotification -> painterResource(DesignR.drawable.ic_tag_fill_blue)
                        else -> painterResource(DesignR.drawable.ic_card_filled_blue)
                    },
                    contentDescription = ""
                )
                Text(
                    text = when (data) {
                        is FeedLikeNotification,
                        is UserCommentLike,
                        is UserCommentWrite,
                            -> stringResource(R.string.home_notice_item_feed_like)

                        is UserBlockNotification,
                        is UserDeleteNotification,
                            -> stringResource(R.string.home_notice_item_limit)

                        is FollowNotification -> stringResource(R.string.home_notice_item_follow)
                        is UserTagNotification -> stringResource(R.string.home_notice_item_tag)
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
                        is UserTagNotification -> data.viewTime
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
                        R.string.home_notice_under_card_like_comment,
                        data.nickName
                    )

                    is UserCommentWrite -> stringResource(
                        R.string.home_notice_under_card_comment,
                        data.nickName
                    )

                    is UserDeleteNotification -> stringResource(R.string.home_notice_delete_card)
                    is UserTagNotification -> stringResource(R.string.home_notice_tag_card_upload , data.tagContent)
                },
                style = TextComponent.TITLE_2_SB_16,
                color = GRAY_600,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 24.dp)
            )
        }
    }

    @Composable
    internal fun NotifyViewRead(data: Notification, onCardClick: (cardId: Long) -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(102.dp)
                .background(color = WHITE)
                .padding(horizontal = 24.dp, vertical = 18.dp)
                .then(
                    if (data is UserCommentWrite) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                onCardClick(data.targetCardId)
                            }
                        )
                    } else {
                        Modifier
                    }
                )
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
                        is FollowNotification -> painterResource(DesignR.drawable.ic_users_filled)
                        is UserBlockNotification,
                        is UserDeleteNotification,
                            -> painterResource(DesignR.drawable.ic_danger)
                        is UserTagNotification -> painterResource(DesignR.drawable.ic_tag_fill_blue)
                        else -> painterResource(DesignR.drawable.ic_card_filled_blue)
                    },
                    contentDescription = ""
                )
                Text(
                    text = when (data) {
                        is FeedLikeNotification,
                        is UserCommentLike,
                        is UserCommentWrite,
                            -> stringResource(R.string.home_notice_item_feed_like)

                        is UserBlockNotification,
                        is UserDeleteNotification,
                            -> stringResource(R.string.home_notice_item_limit)

                        is FollowNotification -> stringResource(R.string.home_notice_item_follow)
                        is UserTagNotification -> stringResource(R.string.home_notice_item_tag)
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
                        is UserTagNotification -> data.viewTime
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
                        R.string.home_notice_under_card_like_comment,
                        data.nickName
                    )

                    is UserCommentWrite -> stringResource(
                        R.string.home_notice_under_card_comment,
                        data.nickName
                    )

                    is UserDeleteNotification -> stringResource(R.string.home_notice_delete_card)
                    is UserTagNotification -> stringResource(
                        R.string.home_notice_tag_card_upload,
                        data.tagContent
                    )
                },
                style = TextComponent.TITLE_2_SB_16,
                color = GRAY_600,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 24.dp)
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