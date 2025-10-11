package com.phew.core_design.component.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.phew.core_design.NeutralColor
import com.phew.core_design.OpacityColor
import com.phew.core_design.Primary
import com.phew.core_design.TextComponent
import com.phew.core_design.component.card.component.BottomContent


/**
 *  TODO
 *  id 값은 현재 Card Id 값을 지정했는데 당장은 필요하지 않으나, 추후 검토하여 제거 가능
 */

@Composable
fun FeedDefaultCard(
    id: String,
    contentText: String,
    imgUrl: String,
    font: String,
    distance: String = "",
    likeCount: String = "0",
    commentCount: String = "0",
    timeAgo: String,
    onClick: () -> Unit
) {
    FeedCardImpl(
        id = id,
        contentText = contentText,
        imgUrl = imgUrl,
        font = font,
        distance = distance,
        likeCount = likeCount,
        commentCount = commentCount,
        timeAgo = timeAgo,
        remainingTimeMillis = 0L,
        onClick = onClick
    )
}

@Composable
fun FeedPungCard(
    id: String,
    contentText: String,
    imgUrl: String = "",
    font: String,
    distance: String,
    likeCount: String = "0",
    commentCount: String = "0",
    timeAgo: String,
    remainingTimeMillis: Long,
    onClick: () -> Unit
) {
    FeedCardImpl(
        id = id,
        contentText = contentText,
        imgUrl = imgUrl,
        font = font,
        distance = distance,
        likeCount = likeCount,
        commentCount = commentCount,
        timeAgo = timeAgo,
        remainingTimeMillis = remainingTimeMillis,
        onClick = onClick
    )
}

@Composable
fun FeedDeletedCard(
    id: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = NeutralColor.GRAY_200,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, NeutralColor.GRAY_100)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 중앙 콘텐츠 영역 (삭제 메시지)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f)
                    .padding(horizontal = 25.dp, vertical = 52.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = NeutralColor.GRAY_600,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .width(279.dp)
                        .heightIn(min = 61.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "카드가 삭제되었어요",
                        style = TextComponent.BODY_1_M_14,
                        color = NeutralColor.WHITE,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 하단 영역 (만료된 타이머 표시)
            BottomContent(
                distance = "",
                likeCount = "0",
                commentCount = "0",
                timeAgo = "",
                remainingTimeMillis = "0" // 만료된 상태를 위해 0으로 설정
            )
        }
    }
}

@Composable
fun FeedAdminCard(
    id: String,
    contentText: String,
    imgUrl: String = "",
    font: String,
    timeAgo: String,
    commentCount: String,
    likeCount: String,
    onClick: () -> Unit
) {
    FeedAdminCardImpl(
        id = id,
        contentText = contentText,
        imgUrl = imgUrl,
        font = font,
        likeCount = likeCount,
        commentCount = commentCount,
        timeAgo = timeAgo,
        onClick = onClick
    )
}

@Composable
private fun FeedCardImpl(
    id: String,
    contentText: String,
    imgUrl: String,
    font: String,
    distance: String = "",
    likeCount: String,
    commentCount: String,
    timeAgo: String,
    remainingTimeMillis: Long,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Primary.MAIN,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, NeutralColor.GRAY_200)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BodyContent(
                contentText = contentText,
                imgUrl = imgUrl,
                fontFamily = resolveFontFamily(font = font)
            )

            BottomContent(
                distance = distance,
                likeCount = likeCount,
                commentCount = commentCount,
                timeAgo = timeAgo,
                remainingTimeMillis = remainingTimeMillis.toString()
            )
        }
    }
}

@Composable
private fun FeedAdminCardImpl(
    id: String,
    contentText: String = "",
    imgUrl: String = "",
    font: String = "",
    likeCount: String = "",
    commentCount: String = "",
    timeAgo: String = "",
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Primary.MAIN,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, NeutralColor.GRAY_200)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BodyContent(
                contentText = contentText,
                imgUrl = imgUrl,
                fontFamily = resolveFontFamily(font = font)
            )

            BottomContent(
                likeCount = likeCount,
                commentCount = commentCount,
                isAdminManger = true,
                timeAgo = timeAgo
            )
        }
    }
}

@Composable
private fun BodyContent(
    modifier: Modifier = Modifier,
    contentText: String = "",
    imgUrl: String = "",
    fontFamily: FontFamily,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2f) // 유지: 가로:세로 = 2:1
    ) {
        AsyncImage(
            model = imgUrl,
            contentDescription = "SOOUM FEED $contentText",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 32.dp)
                .heightIn(min = 103.dp)
                .width(264.dp)
                .background(
                    color = OpacityColor.blackSmallColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp), // 패딩 조정
                text = contentText,
                style = TextComponent.BODY_1_M_14,
                color = NeutralColor.WHITE,
                fontFamily = fontFamily,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Previews
@Composable
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
private fun Preview_FeedCard() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FeedDefaultCard(
            id = "1",
            contentText = "요즘 혼자 걷는 시간이 좋아요",
            imgUrl = "",
            font = "",
            distance = "600m",
            likeCount = "2",
            commentCount = "1",
            timeAgo = "방금 전",
            onClick = {}
        )

        FeedPungCard(
            id = "2",
            contentText = "오늘 하루종일 아무 일도 안 했는데 괜히 더 지치는 기분이야",
            imgUrl = "",
            font = "",
            distance = "600m",
            likeCount = "0",
            commentCount = "5",
            timeAgo = "방금 전",
            remainingTimeMillis = 86400000L,
            onClick = {}
        )

        FeedDeletedCard(
            id = "3",
            onClick = {}
        )

        FeedAdminCard(
            id = "4",
            contentText = "안녕하세요, 숨 팀입니다. 더 나은 서비스를 위해 준비 중입니다.",
            imgUrl = "",
            font = "",
            timeAgo = "방금 전",
            commentCount = "0",
            likeCount = "0",
            onClick = {}
        )
    }
}

private fun resolveFontFamily(font: String): FontFamily {
    // TODO: 폰트 문자열 매핑 규칙 확정 시 교체
    return FontFamily.Default
}
