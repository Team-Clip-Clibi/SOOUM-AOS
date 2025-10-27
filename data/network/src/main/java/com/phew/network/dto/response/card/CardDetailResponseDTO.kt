package com.phew.network.dto.response.card

import kotlinx.serialization.Serializable

@Serializable
data class CardDetailResponseDTO(
    val cardId: Long,
    val likeCnt: Int,
    val commentCardCnt: Int,
    val cardImgUrl: String,
    val cardImgName: String,
    val cardContent: String,
    val font: String,
    val distance: String? = null,
    val createdAt: String,
    val isAdminCard: Boolean,
    val memberId: Long,
    val nickname: String,
    val profileImgUrl: String? = null,
    val isLike: Boolean,
    val isCommentWritten: Boolean,
    val tags: List<CardDetailTagDTO>,
    val isOwnCard: Boolean,
    val previousCardId: String? = null,
    val isPreviousCardDeleted: Boolean = false,
    val previousCardImgUrl: String? = null,
    val visitedCnt: Int,
    val isFeedCard: Boolean,
    val storyExpirationTime : String?
)

@Serializable
data class CardDetailTagDTO(
    val tagId: Long,
    val name: String,
)
