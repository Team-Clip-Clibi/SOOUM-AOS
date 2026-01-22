package com.phew.presentation.detail.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.phew.domain.dto.CardDetail

@Composable
fun TrackCardInteraction(
    cardDetail: CardDetail?,
    onCardChanged: () -> Unit
) {
    var lastSnapshot by remember { mutableStateOf<CardInteractionSnapshot?>(null) }
    LaunchedEffect(cardDetail?.cardId, cardDetail?.likeCount, cardDetail?.commentCardCount) {
        if (cardDetail == null) return@LaunchedEffect
        val snapshot = CardInteractionSnapshot(
            cardId = cardDetail.cardId,
            likeCount = cardDetail.likeCount,
            commentCount = cardDetail.commentCardCount
        )
        val previous = lastSnapshot
        if (previous == null || previous.cardId != snapshot.cardId) {
            lastSnapshot = snapshot
        } else if (previous != snapshot) {
            lastSnapshot = snapshot
            onCardChanged()
        }
    }
}

private data class CardInteractionSnapshot(
    val cardId: Long,
    val likeCount: Int,
    val commentCount: Int
)
