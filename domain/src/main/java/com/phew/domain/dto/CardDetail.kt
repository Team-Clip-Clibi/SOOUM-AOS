package com.phew.domain.dto

data class CardDetail(
    val cardId: Int,
    val likeCount: Int,
    val commentCardCount: Int,
    val cardImgUrl: String,
    val cardImgName: String,
    val cardContent: String,
    val font: String,
    val distance: String?,
    val createdAt: String,
    val isAdminCard: Boolean,
    val memberId: Int,
    val nickname: String,
    val profileImgUrl: String,
    val isLike: Boolean,
    val isCommentWritten: Boolean,
    val tags: List<CardDetailTag>,
    val isOwnCard: Boolean,
    val previousCardId: String?,
    val isPreviousCardDeleted: Boolean,
    val previousCardImgUrl: String?,
    val visitedCnt: Int
)

data class CardDetailTag(
    val tagId: Int,
    val name: String
)

data class CardComment(
    val cardId: Int,
    val likeCount: Int,
    val commentCardCount: Int,
    val cardImgUrl: String,
    val cardImgName: String,
    val cardContent: String,
    val font: String,
    val distance: String?,
    val createdAt: String,
    val isAdminCard: Boolean
)

data class CardReply(
    val isDistanceShared: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val content: String,
    val font: String,
    val imgType: String,
    val imgName: String,
    val tags: List<String>
)

data class CardReplyRequest(
    val isDistanceShared: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val content: String,
    val font: String,
    val imgType: String,
    val imgName: String,
    val tags: List<String>
)
