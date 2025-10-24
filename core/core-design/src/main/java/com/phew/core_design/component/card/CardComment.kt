package com.phew.core_design.component.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary
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
    onClick: () -> Unit
) {

    Surface(
        modifier = Modifier
            .width(211.dp),
        shape = RoundedCornerShape(16.dp),
        color = Primary.MAIN,
        shadowElevation = 6.dp,
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
                imgUrl = thumbnailUri,
                fontFamily = resolveFontFamily(font = font),
                textMaxLines = 4
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