package com.phew.presentation.detail.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phew.core.ui.model.navigation.CardDetailArgs
import com.phew.core_design.NeutralColor
import com.phew.core_design.component.card.CardDetail
import com.phew.presentation.detail.component.CardDetailBottom
import com.phew.presentation.detail.component.CardDetailHeader

@Composable
internal fun CardDetailRoute(
    modifier: Modifier = Modifier,
    args: CardDetailArgs,
    onBackPressed: () -> Unit
) {

}


@Composable
private fun CardDetailScreen(
    modifier: Modifier,

) {
    Scaffold(
        modifier = modifier,
        topBar = {

        }
    ) { paddingValues ->
        val scrollState = rememberScrollState()

        Column(
            modifier = modifier
                .background(NeutralColor.WHITE)
                .fillMaxSize()
                .padding(paddingValues)
        ) {


        }
    }
}

@Preview
@Composable
private fun CardDetailPreview() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
    ) {

        CardDetail(
            previousCommentThumbnailUri = null,
            cardContent = "이건 ReplyCard 예시",
            cardThumbnailUri = "",
            cardTags = listOf("내머리와충돌", "중상", "위동"),
            header = {
                CardDetailHeader(
                    profileUri = "",
                    nickName = "닉네임",
                    distance = "10km",
                    createAt = "2025-10-09T03:54:10.026919"
                )
            },
            bottom = {
                CardDetailBottom(
                    likeCnt = 10,
                    commentCnt = 10,
                    searchCnt = 10,
                    isLikeCard = true,
                    onClickLike = {},
                    onClickComment = {}
                )
            }
        )

        Spacer(modifier = Modifier.size(4.dp))

        CardDetail(
            previousCommentThumbnailUri = "null",
            isDeleted = true,
            cardContent = "",
            cardThumbnailUri = "",
            cardTags = listOf("내머리와충돌", "중상", "위동"),
            header = {
                CardDetailHeader(
                    profileUri = "",
                    nickName = "닉네임",
                    distance = "10km",
                    createAt = "2025-10-09T03:54:10.026919"
                )
            },
            bottom = {

            }
        )
        Spacer(modifier = Modifier.size(4.dp))

        CardDetail(
            previousCommentThumbnailUri = "null",
            cardContent = "이건 ReplyCard 예시",
            cardThumbnailUri = "",
            cardTags = listOf("내머리와충돌", "중상", "위동"),
            header = {
                CardDetailHeader(
                    profileUri = "",
                    nickName = "닉네임",
                    distance = "10km",
                    createAt = "2025-10-09T03:54:10.026919"
                )
            },
            bottom = {
                CardDetailBottom(
                    likeCnt = 10,
                    commentCnt = 10,
                    searchCnt = 10,
                    isLikeCard = true,
                    onClickLike = {},
                    onClickComment = {}
                )
            }
        )

        Spacer(modifier = Modifier.size(24.dp))
    }
}