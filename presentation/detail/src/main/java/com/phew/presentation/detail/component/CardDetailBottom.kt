package com.phew.presentation.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phew.core_design.Danger
import com.phew.core_design.NeutralColor
import com.phew.core_design.R
import com.phew.presentation.detail.R as detailR
import com.phew.core_design.TextComponent
import com.phew.core_design.component.button.IconButtons

@Composable
internal fun CardDetailBottom(
    modifier: Modifier = Modifier,
    likeCnt: Int,
    commentCnt: Int,
    searchCnt: Int,
    isLikeCard: Boolean,
    onClickLike: () -> Unit,
    onClickComment: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(NeutralColor.WHITE),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(2f)
                .fillMaxWidth()
        ) {
            IconButtons(
                enabled = !isLikeCard,
                selectedIconTintColor = if (isLikeCard) Danger.M_RED else NeutralColor.GRAY_500,
                selectedIconId = R.drawable.ic_heart_filled,
                unSelectedIconId = R.drawable.ic_heart_stoke,
                buttonText = likeCnt.toString(),
                onClick = onClickLike
            )

            IconButtons(
                selectedIconTintColor = NeutralColor.GRAY_500,
                selectedIconId = R.drawable.ic_message_stoke,
                unSelectedIconId = R.drawable.ic_message_stoke,
                buttonText = commentCnt.toString(),
                onClick = onClickComment
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(end = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = stringResource(detailR.string.card_detail_bottom_search),
                style = TextComponent.CAPTION_1_SB_12,
                color = NeutralColor.GRAY_500
            )
            Spacer(Modifier.width(2.dp))
            Text (
                text = searchCnt.toString(),
                style = TextComponent.CAPTION_1_SB_12,
                color = NeutralColor.GRAY_500
            )
        }
    }
}

@Preview
@Composable
private fun CardDetailBottomPreview() {
    CardDetailBottom(
        likeCnt = 10,
        commentCnt = 10,
        searchCnt = 10,
        isLikeCard = true,
        onClickLike = {},
        onClickComment = {}
    )
}