package com.phew.core_design.component.card.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.phew.core_common.TimeUtils
import com.phew.core_common.log.SooumLog
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
            painter = painterResource(R.drawable.ic_bomb),
            contentDescription = "Time Limit card",
            modifier = Modifier.size(12.dp)
        )
        if (isExpired) {
            Text(
                text = "00:00:00",
                style = TextComponent.CAPTION_2_M_12,
                color = Primary.DARK,
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
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = "sooum",
            style = TextComponent.CAPTION_2_M_12,
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
            Image(
                painter = painterResource(R.drawable.ic_location_stoke),
                modifier = Modifier.size(12.dp),
                contentDescription = "location",
            )
            Text(
                text = location,
                style = TextComponent.CAPTION_2_M_12,
                color = NeutralColor.GRAY_500,
                modifier = Modifier.padding(start = 2.dp)
            )
            if (!writeTime.isNullOrEmpty()) {
                SpotSeparator()
            }
        }
        if (!writeTime.isNullOrEmpty()) {
            Text(
                text = writeTime,
                style = TextComponent.CAPTION_2_M_12,
                color = NeutralColor.GRAY_500,
                modifier = Modifier
            )
        }
    }
}


@Composable
internal fun LikeAndComment(
    modifier: Modifier = Modifier,
    likeValue: String? = null,
    commentValue: String? = null
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 좋아요 버튼
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(R.drawable.ic_heart_stoke),
                contentDescription = "좋아요",
                modifier = modifier.then(
                    Modifier.size(12.dp)
                )
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = likeValue ?: "0",
                style = TextComponent.CAPTION_2_M_12
            )
        }

        // 댓글 버튼
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.then(
                Modifier.padding(start = 4.dp)
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_message_stoke),
                contentDescription = "댓글",
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = commentValue ?: "0",
                style = TextComponent.CAPTION_2_M_12
            )
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

@Composable
internal fun BottomContent(
    modifier: Modifier = Modifier,
    distance: String? = null,
    likeCount: String? = null,
    commentCount: String? = null,
    timeAgo: String? = null,
    remainingTimeMillis: String? = null,
    isAdminManger: Boolean = false
) {
    val remaining = remainingTimeMillis?.toLongOrNull() ?: 0L
    val showTimer = !remainingTimeMillis.isNullOrEmpty() && remaining > 0L
    val isExpired = remaining <= 0L

    SooumLog.d(TAG, "remainingTimeMillis : $remainingTimeMillis, " +
            "likeCount : $likeCount, commentCount : $commentCount, isExpired : $isExpired, showTimer: $showTimer")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(color = NeutralColor.WHITE)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showTimer) {
                TimerLabel(remainingTimeMillis = remaining, isExpired = isExpired)
                if (!distance.isNullOrEmpty() || !timeAgo.isNullOrEmpty()) {
                    SpotSeparator()
                }
            }
            if (isAdminManger){
                ManagerLabel()
                if (!distance.isNullOrEmpty() || !timeAgo.isNullOrEmpty()) {
                    SpotSeparator()
                }
            }
            LocationAndWriteTimeLabel(
                location = distance?.takeIf { it.isNotEmpty() },
                writeTime = timeAgo?.takeIf { it.isNotEmpty() }
            )
        }
        if (!showTimer) {
            SooumLog.d(TAG, "LikeAndComment")
            LikeAndComment(
                likeValue = likeCount,
                commentValue = commentCount
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun BottomContentPreview_WithTimer() {
    Box(modifier = Modifier.background(NeutralColor.WHITE)) {
        BottomContent(
            distance = "600m",
            likeCount = "0",
            commentCount = "0",
            timeAgo = "방금 전",
            remainingTimeMillis = "86400000"
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
            timeAgo = "방금 전",
            remainingTimeMillis = null
        )
    }
}

private const val TAG = "BottomContent"