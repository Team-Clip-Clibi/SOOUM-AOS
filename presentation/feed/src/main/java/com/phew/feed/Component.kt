package com.phew.feed

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.phew.core_common.TimeUtils
import com.phew.core_design.NeutralColor
import com.phew.core_design.OpacityColor
import com.phew.core_design.Primary
import com.phew.core_design.TextComponent
import com.phew.core_design.component.tab.SooumTab
import com.phew.core_design.component.tab.SooumTabRow
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
import kotlinx.coroutines.delay

object FeedUi {
    // TODO 임시.. 어떤 데이터가 오는지 어떻게 매칭 해야할지 모르겠음..
    @Composable
    internal fun getTextStyleForFont(font: String) = when (font.lowercase()) {
        "bold", "pretendard-bold" -> TextComponent.TITLE_2_SB_16
        "semi_bold", "semibold", "pretendard-semibold" -> TextComponent.SUBTITLE_1_M_16
        "medium", "pretendard-medium" -> TextComponent.BODY_1_M_14
        "regular", "pretendard-regular", "default" -> TextComponent.BODY_1_M_14
        "light", "pretendard-light" -> TextComponent.CAPTION_2_M_12
        "extra_bold", "extrabold", "pretendard-extrabold" -> TextComponent.HEAD_3_B_20
        "black", "pretendard-black" -> TextComponent.HEAD_2_B_24
        else -> TextComponent.BODY_1_M_14 // fallback to default
    }

    @Composable
    internal fun AnimatedFeedTabLayout(
        selectTabData: Int,
        recentClick: () -> Unit,
        popularClick: () -> Unit,
        nearClick: () -> Unit,
        isTabsVisible: Boolean,
        onDistanceClick: (Int) -> Unit,
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
                    val selectDistance by remember { mutableIntStateOf(DISTANCE_1KM) }
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
    fun TemporaryCard(
        limitedTime: String,
        location: String,
        writeTime: String,
        commentValue: String,
        likeValue: String,
        uri: Uri,
        content: String,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(206.dp)
                .border(
                    width = 1.dp,
                    color = NeutralColor.GRAY_100,
                    shape = RoundedCornerShape(size = 16.dp)
                )
                .clip(RoundedCornerShape(size = 16.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(172.dp)
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = "SOOUM FEED $content",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, top = 32.dp, end = 32.dp, bottom = 32.dp)
                        .height(82.dp)
                        .background(
                            color = OpacityColor.blackSmallColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .align(Alignment.Center),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = content,
                        style = TextComponent.BODY_1_M_14,
                        color = NeutralColor.WHITE,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(color = NeutralColor.WHITE)
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(com.phew.core_design.R.drawable.ic_bomb),
                    contentDescription = "Time Limit card : $limitedTime",
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = limitedTime,
                    style = TextComponent.CAPTION_2_M_12,
                    color = Primary.DARK,
                    modifier = Modifier.padding(start = 2.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Image(
                    painter = painterResource(com.phew.core_design.R.drawable.ic_spot),
                    contentDescription = "Time Limit card : $limitedTime",
                )
                Spacer(modifier = Modifier.width(4.dp))
                Image(
                    painter = painterResource(com.phew.core_design.R.drawable.ic_location_stoke),
                    modifier = Modifier.size(14.dp),
                    contentDescription = "location $location",
                )
                Text(
                    text = location,
                    style = TextComponent.CAPTION_2_M_12,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.padding(start = 2.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Image(
                    painter = painterResource(com.phew.core_design.R.drawable.ic_spot),
                    contentDescription = "Time Limit card : $limitedTime",
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = writeTime,
                    style = TextComponent.CAPTION_2_M_12,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.weight(1f)
                )
                Image(
                    painter = painterResource(com.phew.core_design.R.drawable.ic_message_stoke),
                    contentDescription = "comment $commentValue",
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = commentValue,
                    style = TextComponent.CAPTION_2_M_12,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                Image(
                    painter = painterResource(com.phew.core_design.R.drawable.ic_heart_stoke),
                    contentDescription = "like $likeValue",
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = likeValue,
                    style = TextComponent.CAPTION_2_M_12,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }

    @Composable
    fun FeedCardView(
        location: String,
        writeTime: String,
        commentValue: String,
        likeValue: String,
        uri: Uri,
        content: String,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(206.dp)
                .border(
                    width = 1.dp,
                    color = NeutralColor.GRAY_100,
                    shape = RoundedCornerShape(size = 16.dp)
                )
                .clip(RoundedCornerShape(size = 16.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(172.dp)
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = "SOOUM FEED $content",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, top = 32.dp, end = 32.dp, bottom = 32.dp)
                        .height(82.dp)
                        .background(
                            color = OpacityColor.blackSmallColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .align(Alignment.Center),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = content,
                        style = TextComponent.BODY_1_M_14,
                        color = NeutralColor.WHITE,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(color = NeutralColor.WHITE)
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(com.phew.core_design.R.drawable.ic_location_stoke),
                    modifier = Modifier.size(14.dp),
                    contentDescription = "location $location",
                )
                Text(
                    text = location,
                    style = TextComponent.CAPTION_2_M_12,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Image(
                painter = painterResource(com.phew.core_design.R.drawable.ic_spot),
                contentDescription = "Time Limit card : $location",
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = writeTime,
                style = TextComponent.CAPTION_2_M_12,
                color = NeutralColor.GRAY_500,
                modifier = Modifier.weight(1f)
            )
            Image(
                painter = painterResource(com.phew.core_design.R.drawable.ic_message_stoke),
                contentDescription = "comment $commentValue",
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = commentValue,
                style = TextComponent.CAPTION_2_M_12,
                color = NeutralColor.GRAY_500,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
            Image(
                painter = painterResource(com.phew.core_design.R.drawable.ic_heart_stoke),
                contentDescription = "like $likeValue",
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = likeValue,
                style = TextComponent.CAPTION_2_M_12,
                color = NeutralColor.GRAY_500,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }

    @Composable
    internal fun TypedFeedCardView(
        feedCard: FeedCardType,
        onRemoveCard: (String) -> Unit = {}
    ) {
        when (feedCard) {
            is FeedCardType.BoombType -> BoombTypeCard(
                feedCard = feedCard,
                onRemoveCard = onRemoveCard
            )

            is FeedCardType.AdminType -> AdminTypeCard(feedCard)
            is FeedCardType.NormalType -> NormalTypeCard(feedCard)
        }
    }

    @Composable
    internal fun BoombTypeCard(
        feedCard: FeedCardType.BoombType,
        onRemoveCard: (String) -> Unit
    ) {
        var remainingTimeMillis by remember {
            mutableLongStateOf(TimeUtils.parseTimerToMillis(feedCard.storyExpirationTime ?: ""))
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isExpired) 240.dp else 206.dp)
                .clip(RoundedCornerShape(size = 16.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(172.dp)
            ) {
                AsyncImage(
                    model = feedCard.imageUrl,
                    contentDescription = "SOOUM FEED ${feedCard.content}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, top = 32.dp, end = 32.dp, bottom = 32.dp)
                        .height(82.dp)
                        .background(
                            color = OpacityColor.blackSmallColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .align(Alignment.Center),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = feedCard.content,
                        style = getTextStyleForFont(feedCard.font),
                        color = NeutralColor.WHITE,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(color = NeutralColor.WHITE)
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(com.phew.core_design.R.drawable.ic_bomb),
                    contentDescription = "Time Limit card",
                    modifier = Modifier.size(16.dp)
                )
                if (isExpired) {
                    Text(
                        text = "00:00:00",
                        style = TextComponent.CAPTION_2_M_12,
                        color = NeutralColor.GRAY_400,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                } else {
                    Text(
                        text = TimeUtils.formatMillisToTimer(remainingTimeMillis),
                        style = TextComponent.CAPTION_2_M_12,
                        color = Primary.DARK,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Image(
                    painter = painterResource(com.phew.core_design.R.drawable.ic_spot),
                    contentDescription = "Spot separator",
                )
                Spacer(modifier = Modifier.width(4.dp))

                if (!feedCard.location.isNullOrEmpty()) {
                    // TODO 분리 필요
                    Image(
                        painter = painterResource(com.phew.core_design.R.drawable.ic_location_stoke),
                        modifier = Modifier.size(14.dp),
                        contentDescription = "location ${feedCard.location}",
                    )

                    Text(
                        text = feedCard.location ?: "",
                        style = TextComponent.CAPTION_2_M_12,
                        color = NeutralColor.GRAY_500,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Image(
                    painter = painterResource(com.phew.core_design.R.drawable.ic_spot),
                    contentDescription = "Spot separator",
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = feedCard.writeTime,
                    style = TextComponent.CAPTION_2_M_12,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.weight(1f)
                )
                Image(
                    painter = painterResource(com.phew.core_design.R.drawable.ic_message_stoke),
                    contentDescription = "comment ${feedCard.commentValue}",
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = feedCard.commentValue,
                    style = TextComponent.CAPTION_2_M_12,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                Image(
                    painter = painterResource(com.phew.core_design.R.drawable.ic_heart_stoke),
                    contentDescription = "like ${feedCard.likeValue}",
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = feedCard.likeValue,
                    style = TextComponent.CAPTION_2_M_12,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }

            // 만료된 경우 삭제 메시지 표시
            if (isExpired) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = NeutralColor.GRAY_100)
                        .clickable { onRemoveCard(feedCard.cardId) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "카드가 삭제되었습니다. 삭제되었어요",
                        style = TextComponent.CAPTION_2_M_12,
                        color = NeutralColor.GRAY_500,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    @Composable
    internal fun AdminTypeCard(feedCard: FeedCardType.AdminType) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(206.dp)
                .clip(RoundedCornerShape(size = 16.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(172.dp)
            ) {
                AsyncImage(
                    model = feedCard.imageUrl,
                    contentDescription = "SOOUM ADMIN FEED ${feedCard.content}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, top = 32.dp, end = 32.dp, bottom = 32.dp)
                        .height(82.dp)
                        .background(
                            color = OpacityColor.blackSmallColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .align(Alignment.Center),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = feedCard.content,
                        style = getTextStyleForFont(feedCard.font),
                        color = NeutralColor.WHITE,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(color = Primary.LIGHT_1)
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(com.phew.core_design.R.drawable.ic_official_filled),
                    contentDescription = "Time Limit card",
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "sooum",
                    style = TextComponent.CAPTION_2_M_12,
                    color = NeutralColor.BLACK,
                    modifier = Modifier.padding(start = 2.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Image(
                    painter = painterResource(com.phew.core_design.R.drawable.ic_spot),
                    contentDescription = "Spot separator",
                )
                Spacer(modifier = Modifier.width(4.dp))
                // TODO Location 처리 여부에 따른 분기 View 하도록 분리 필요
                if (!feedCard.location.isNullOrEmpty()) {
                    Image(
                        painter = painterResource(com.phew.core_design.R.drawable.ic_location_stoke),
                        modifier = Modifier.size(14.dp),
                        contentDescription = "location ${feedCard.location}",
                    )
                    Text(
                        text = feedCard.location ?: "",
                        style = TextComponent.CAPTION_2_M_12,
                        color = NeutralColor.GRAY_500,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }


                Text(
                    text = feedCard.writeTime,
                    style = TextComponent.CAPTION_2_M_12,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.weight(1f)
                )
                Image(
                    painter = painterResource(com.phew.core_design.R.drawable.ic_message_stoke),
                    contentDescription = "comment ${feedCard.commentValue}",
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = feedCard.commentValue,
                    style = TextComponent.CAPTION_2_M_12,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                Image(
                    painter = painterResource(com.phew.core_design.R.drawable.ic_heart_stoke),
                    contentDescription = "like ${feedCard.likeValue}",
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = feedCard.likeValue,
                    style = TextComponent.CAPTION_2_M_12,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }

    @Composable
    internal fun NormalTypeCard(feedCard: FeedCardType.NormalType) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(206.dp)
                .clip(RoundedCornerShape(size = 16.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(172.dp)
            ) {
                AsyncImage(
                    model = feedCard.imageUrl,
                    contentDescription = "SOOUM FEED ${feedCard.content}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, top = 32.dp, end = 32.dp, bottom = 32.dp)
                        .height(82.dp)
                        .background(
                            color = OpacityColor.blackSmallColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .align(Alignment.Center),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = feedCard.content,
                        style = getTextStyleForFont(feedCard.font),
                        color = NeutralColor.WHITE,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(color = NeutralColor.WHITE)
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!feedCard.location.isNullOrEmpty()) {
                    // TODO 분리 필요
                    Image(
                        painter = painterResource(com.phew.core_design.R.drawable.ic_location_stoke),
                        modifier = Modifier.size(14.dp),
                        contentDescription = "location ${feedCard.location}",
                    )

                    Text(
                        text = feedCard.location ?: "",
                        style = TextComponent.CAPTION_2_M_12,
                        color = NeutralColor.GRAY_500,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Image(
                        painter = painterResource(com.phew.core_design.R.drawable.ic_spot),
                        contentDescription = "Spot separator",
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = TimeUtils.getRelativeTimeString(feedCard.writeTime),
                    style = TextComponent.CAPTION_2_M_12,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.weight(1f)
                )
                Image(
                    painter = painterResource(com.phew.core_design.R.drawable.ic_message_stoke),
                    contentDescription = "comment ${feedCard.commentValue}",
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = feedCard.commentValue,
                    style = TextComponent.CAPTION_2_M_12,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                Image(
                    painter = painterResource(com.phew.core_design.R.drawable.ic_heart_stoke),
                    contentDescription = "like ${feedCard.likeValue}",
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = feedCard.likeValue,
                    style = TextComponent.CAPTION_2_M_12,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }
}

object NotificationUi {

    @Composable
    internal fun AnimatedNoticeTabLayout(
        selectTabData: Int,
        allClick: () -> Unit,
        noticeClick: () -> Unit,
        isTabsVisible: Boolean,
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
                    color = NeutralColor.GRAY_400,
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
                    color = NeutralColor.GRAY_400
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
                color = NeutralColor.GRAY_600,
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
                    color = NeutralColor.GRAY_400,
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
                    color = NeutralColor.GRAY_400
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
                color = NeutralColor.GRAY_600,
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

    TypedFeedCardView(feedCard = sampleAdminCard)
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

    TypedFeedCardView(feedCard = sampleNormalCard)
}