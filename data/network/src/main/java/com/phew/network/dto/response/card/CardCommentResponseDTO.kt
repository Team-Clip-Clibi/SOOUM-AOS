package com.phew.network.dto.response.card

import kotlinx.serialization.Serializable

@Serializable
data class CardCommentResponseDTO(
    val cardId: Int,
    val likeCnt: Int,
    val commentCardCnt: Int,
    val cardImgUrl: String,
    val cardImgName: String,
    val cardContent: String,
    val font: String,
    val distance: String? = null,
    val createdAt: String,
    val isAdminCard: Boolean
)
