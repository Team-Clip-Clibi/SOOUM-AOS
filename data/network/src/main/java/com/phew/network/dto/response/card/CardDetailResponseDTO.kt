package com.phew.network.dto.response.card

import kotlinx.serialization.Serializable

@Serializable
data class CardDetailResponseDTO(
    val cardId: Int,
    val likeCnt: Int,
    val commentCardCnt: Int,
    val cardImgUrl: String,
    val cardImgName: String,
    val cardContent: String,
    val font: String,
    val distance: String? = null,
    val createdAt: String,
    val isAdminCard: Boolean,
    val memberId: Int,
    val nickname: String,
    val profileImgUrl: String,
    val isLike: Boolean,
    val isCommentWritten: Boolean,
    val tags: List<CardDetailTagDTO>,
    val isOwnCard: Boolean,
    val previousCardId: String? = null,
    val isPreviousCardDeleted: Boolean,
    val previousCardImgUrl: String? = null,
    val visitedCnt: Int
)

@Serializable
data class CardDetailTagDTO(
    val tagId: Int,
    val name: String
)
