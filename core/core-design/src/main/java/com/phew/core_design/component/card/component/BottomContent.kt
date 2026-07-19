package com.phew.core_design.component.card.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phew.core_common.TimeUtils
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary
import com.phew.core_design.R
import com.phew.core_design.TextComponent


@Composable
internal fun TimerLabel(
    remainingTimeMillis: Long,
    isExpired: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_timer_stoke),
            contentDescription = "Time Limit card",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        if (isExpired) {
            Text(
                text = "00:00:00",
                style = TextComponent.CAPTION_2_M_12,
                color = Primary.DARK
            )
        } else {
            Text(
                text = TimeUtils.formatMillisToTimer(remainingTimeMillis),
                style = TextComponent.CAPTION_2_M_12,
                color = Primary.DARK
            )
        }
    }
}

@Composable
internal fun ManagerLabel(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_official_filled),
            contentDescription = "Admin",
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "sooum",
            style = TextComponent.BODY_1_M_14,
            color = NeutralColor.BLACK,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}

@Composable
internal fun LocationAndWriteTimeLabel(
    modifier: Modifier = Modifier,
    location: String? = null,
    writeTime: String? = null
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!location.isNullOrEmpty()) {
            Icon(
                painter = painterResource(R.drawable.ic_location_stoke),
                modifier = Modifier.size(12.dp),
                contentDescription = "location",
                tint = NeutralColor.GRAY_500
            )
            Text(
                text = location,
                style = TextComponent.CAPTION_2_M_12.copy(color = NeutralColor.GRAY_500),
                modifier = Modifier.padding(start = 2.dp)
            )
            if (!writeTime.isNullOrEmpty()) {
                SpotSeparator()
            }
        }
        if (!writeTime.isNullOrEmpty()) {
            Text(
                text = TimeUtils.getRelativeTimeString(writeTime),
                style = TextComponent.CAPTION_2_M_12.copy(color = NeutralColor.GRAY_500),
                modifier = Modifier
            )
        }
    }
}


@Composable
internal fun LikeAndComment(
    modifier: Modifier = Modifier,
    likeValue: String? = null,
    commentValue: String? = null,
    pollVoterValue: String? = null,
    isLike: Boolean = false,
    isLikeLoading: Boolean = false,
    likeAnimationKey: Int = 0,
    onClickLike: () -> Unit = {},
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LikeButton(
            likeCount = likeValue ?: "0",
            isLike = isLike,
            isLikeLoading = isLikeLoading,
            likeAnimationKey = likeAnimationKey,
            onClickLike = onClickLike,
        )

        // 댓글 버튼
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_message_stoke),
                contentDescription = "댓글",
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(NeutralColor.GRAY_500)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = commentValue ?: "0",
                style = TextComponent.BODY_1_M_14.copy(color = NeutralColor.GRAY_500),
                color = NeutralColor.GRAY_500
            )
        }
        val visiblePollVoterValue = pollVoterValue
            ?.toLongOrNull()
            ?.takeIf { it >= 0L }
            ?.toString()
        if (visiblePollVoterValue != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_vote_stoke),
                    contentDescription = "투표",
                    modifier = Modifier.size(20.dp),
                    colorFilter = ColorFilter.tint(NeutralColor.GRAY_500)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = visiblePollVoterValue,
                    style = TextComponent.BODY_1_M_14.copy(color = NeutralColor.GRAY_500),
                    color = NeutralColor.GRAY_500
                )
            }
        }
    }
}

@Composable
internal fun SpotSeparator() {
    Spacer(modifier = Modifier.width(4.dp))
    Image(
        painter = painterResource(R.drawable.ic_spot),
        contentDescription = "Spot separator",
    )
    Spacer(modifier = Modifier.width(4.dp))
}

enum class FeedCardType {
    DEFAULT,
    PUNG,
    DELETED,
    ADMIN
}

@Composable
internal fun BottomContent(
    modifier: Modifier = Modifier,
    distance: String? = null,
    likeCount: String? = null,
    commentCount: String? = null,
    pollVoterCount: String? = null,
    timeAgo: String? = null,
    remainingTimeMillis: String? = null,
    isLike: Boolean = false,
    isLikeLoading: Boolean = false,
    likeAnimationKey: Int = 0,
    isAdminManger: Boolean = false,
    showLocationAndTime: Boolean = true,
    cardType: FeedCardType = FeedCardType.DEFAULT,
    onClickLike: () -> Unit = {},
) {
    val remaining = remainingTimeMillis?.toLongOrNull() ?: 0L
    val isExpired = remaining <= 0L
    
    // 작성 시간/거리는 숨기고, 메트릭은 좌측, 스토리 타이머는 우측에 함께 배치한다.
    val showTimer = when (cardType) {
        FeedCardType.PUNG -> !remainingTimeMillis.isNullOrEmpty() && remaining > 0L
        FeedCardType.DELETED -> !remainingTimeMillis.isNullOrEmpty()
        else -> false
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(color = NeutralColor.WHITE)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            isAdminManger -> ManagerLabel()
            cardType == FeedCardType.DELETED -> TimerLabel(
                remainingTimeMillis = remaining,
                isExpired = isExpired
            )
            else -> {
                LikeAndComment(
                    likeValue = likeCount,
                    commentValue = commentCount,
                    pollVoterValue = pollVoterCount,
                    isLike = isLike,
                    isLikeLoading = isLikeLoading,
                    likeAnimationKey = likeAnimationKey,
                    onClickLike = onClickLike,
                )
                if (showTimer) {
                    Spacer(modifier = Modifier.weight(1f))
                    TimerLabel(remainingTimeMillis = remaining, isExpired = isExpired)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun BottomContentPreview_PungCard() {
    Box(modifier = Modifier.background(NeutralColor.WHITE)) {
        BottomContent(
            distance = "600m",
            likeCount = "0",
            commentCount = "0",
            pollVoterCount = "24",
            timeAgo = "방금 전",
            remainingTimeMillis = "86400000",
            cardType = FeedCardType.PUNG
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun BottomContentPreview_NoTimer() {
    Box(modifier = Modifier.background(NeutralColor.WHITE)) {
        BottomContent(
            distance = "600m",
            likeCount = "12",
            commentCount = "3",
            pollVoterCount = "47",
            timeAgo = "방금 전1",
            remainingTimeMillis = null,
            cardType = FeedCardType.DEFAULT
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun BottomContentPreview_DeletedCard() {
    Box(modifier = Modifier.background(NeutralColor.WHITE)) {
        BottomContent(
            distance = "",
            likeCount = null,
            commentCount = null,
            timeAgo = "",
            remainingTimeMillis = "0",
            showLocationAndTime = false,
            cardType = FeedCardType.DELETED
        )
    }
}
