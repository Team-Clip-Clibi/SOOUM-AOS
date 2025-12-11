package com.phew.core_design.component.card

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.phew.core_design.NeutralColor
import com.phew.core_design.OpacityColor
import com.phew.core_design.R
import com.phew.core_design.TextComponent
import com.phew.core_design.UnKnowColor
import com.phew.core_design.component.card.component.BottomContent

@Composable
fun CardViewComment(
    modifier: Modifier = Modifier,
    contentText: String,
    thumbnailUri: String,
    distance: String,
    createAt: String,
    likeCnt: String,
    commentCnt: String,
    font: String,
    onClick: () -> Unit
) {
    CardViewCommentImpl(
        modifier = modifier,
        contentText = contentText,
        thumbnailUri = thumbnailUri,
        distance = distance,
        createAt = createAt,
        likeCnt = likeCnt,
        commentCnt = commentCnt,
        font = font,
        onClick = onClick
    )
}

@Composable
private fun CardViewCommentImpl(
    modifier: Modifier,
    contentText: String,
    thumbnailUri: String,
    distance: String,
    createAt: String,
    likeCnt: String,
    commentCnt: String,
    font: String,
    onClick: () -> Unit
) {

    Surface(
        modifier = modifier
            .fillMaxSize()
            .border(
                width = 1.dp,
                color = NeutralColor.GRAY_100,
                shape = RoundedCornerShape(size = 16.dp)
            )
            .shadow(
                elevation = 16.dp,
                spotColor = UnKnowColor.color,
                ambientColor = UnKnowColor.color
            )
            .clip(shape = RoundedCornerShape(size = 16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onClick() }
                ),
        ) {
            BodyContent(
                modifier = Modifier.weight(1f),
                contentText = contentText,
                imgUrl = thumbnailUri,
                font = font,
                textMaxLines = 4,
                useFixedHeight = false
            )

            BottomContent(
                distance = distance,
                likeCount = likeCnt,
                commentCount = commentCnt,
                timeAgo = createAt
            )
        }
    }
}

@Composable
fun CommentBodyContent(
    modifier: Modifier = Modifier,
    contentText: String = "",
    imgUrl: String = "",
    fontFamily: FontFamily,
    textMaxLines: Int,
    cardId: Long,
    onClick: (Long) -> Unit,
) {
    Box(
        modifier = modifier
            .widthIn(min = 119.dp)
            .heightIn(min = 119.dp)
            .aspectRatio(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    onClick(cardId)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imgUrl)
                .crossfade(true)
                .build(),
            contentDescription = "SOOUM Comment $contentText",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(NeutralColor.WHITE)
                )
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(NeutralColor.WHITE)
                )
            }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .background(
                    color = OpacityColor.blackSmallColor,
                    shape = RoundedCornerShape(5.dp)
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = contentText,
                style = TextComponent.CAPTION_4_M_5.copy(
                    color = NeutralColor.WHITE,
                    fontFamily = fontFamily,
                    textAlign = TextAlign.Center
                ),
                maxLines = textMaxLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
    }
}
