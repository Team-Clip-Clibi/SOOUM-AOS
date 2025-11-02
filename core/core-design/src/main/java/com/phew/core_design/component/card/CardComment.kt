package com.phew.core_design.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.phew.core_design.NeutralColor
import com.phew.core_design.OpacityColor
import com.phew.core_design.TextComponent
import com.phew.core_design.UnKnowColor
import com.phew.core_design.component.card.component.BottomContent

@Composable
fun CardViewComment(
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
    contentText: String,
    thumbnailUri: String,
    distance: String,
    createAt: String,
    likeCnt: String,
    commentCnt: String,
    font: String,
    onClick: () -> Unit,
) {

    Surface(
        modifier = Modifier
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
                fontFamily = resolveFontFamily(font = font),
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
private fun CommentBodyContent(
    modifier: Modifier = Modifier,
    contentText: String = "",
    imgUrl: String = "",
    fontFamily: FontFamily,
    textMaxLines: Int
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp)
    ) {
        AsyncImage(
            model = imgUrl,
            contentDescription = "SOOUM Comment $contentText",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(
                    color = OpacityColor.blackSmallColor,
                    shape = RoundedCornerShape(12.dp)
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = contentText,
                style = TextComponent.BODY_1_M_14.copy(
                    color = NeutralColor.WHITE,
                    fontFamily = fontFamily,
                    textAlign = TextAlign.Center
                ),
                maxLines = textMaxLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
        }
    }
}