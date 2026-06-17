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
    val isFeedCard: Boolean = false,
    val poll: PollResponseDTO? = null,
    val storyExpirationTime : String?
)

@Serializable
data class CardDetailTagDTO(
    val tagId: Long,
    val name: String,
)

@Serializable
data class PollResponseDTO(
    val totalVoterCnt: Long,
    val isVoted: Boolean,
    val options: List<PollOptionResponseDTO>
)

@Serializable
data class PollOptionResponseDTO(
    val pollOptionId: Long,
    val content: String,
    val voteCnt: Long? = null,
    val votePercentage: Double? = null,
    val isVoted: Boolean
)

@Serializable
data class PollVoteResponseDTO(
    val feedCardId: Long,
    val pollId: Long,
    val totalVoterCnt: Long,
    val options: List<PollOptionResponseDTO>
)
