package com.phew.domain.dto

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


data class CardDetail(
    val cardId: Long,
    val likeCount: Int,
    val commentCardCount: Int,
    val cardImgUrl: String,
    val cardImgName: String,
    val cardContent: String,
    val font: String,
    val distance: String?,
    val createdAt: String,
    val isAdminCard: Boolean,
    val memberId: Long,
    val nickname: String,
    val profileImgUrl: String?,
    val isLike: Boolean,
    val isCommentWritten: Boolean,
    val tags: List<CardDetailTag>,
    val isOwnCard: Boolean,
    val previousCardId: String?,
    val previousCardImgUrl: String?,
    val visitedCnt: Int,
    val isFeedCard: Boolean = false,
    val storyExpirationTime: String?,
) {
    val endTime = storyExpirationTime?.endTimeMillisecondTime()
    private fun String?.endTimeMillisecondTime(): Long {
        if (this.isNullOrBlank()) return 0L
        val expireAt = parseIsoToEpochMillis(this) ?: return 0L
        val currentTime = System.currentTimeMillis()
        return (expireAt - currentTime).coerceAtLeast(0L)
    }

    private fun parseIsoToEpochMillis(value: String): Long? {
        val defaultZone = ZoneId.of("Asia/Seoul")
        return runCatching {
            OffsetDateTime.parse(value).toInstant().toEpochMilli()
        }.getOrNull()
            ?: runCatching {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
                LocalDateTime.parse(value, formatter)
                    .atZone(defaultZone)
                    .toInstant()
                    .toEpochMilli()
            }.getOrNull()
            ?: runCatching {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
                LocalDateTime.parse(value, formatter)
                    .atZone(defaultZone)
                    .toInstant()
                    .toEpochMilli()
            }.getOrNull()
    }
}

data class CardDetailTag(
    val tagId: Long,
    val name: String
)

data class CardComment(
    val cardId: Long,
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
    val imgType: String, // TODO Image 서버로 받아오는 로직 구현 필요
    val imgName: String,
    val tags: List<String>
)
