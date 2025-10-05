package com.phew.repository.mapper

import com.phew.domain.dto.FeedLikeNotification
import com.phew.domain.dto.FollowNotification
import com.phew.domain.dto.Latest
import com.phew.domain.dto.Notification
import com.phew.domain.dto.Popular
import com.phew.domain.dto.UserBlockNotification
import com.phew.domain.dto.UserCommentLike
import com.phew.domain.dto.UserCommentWrite
import com.phew.domain.dto.UserDeleteNotification
import com.phew.network.dto.NotificationDTO
import com.phew.network.dto.response.LatestDto
import com.phew.network.dto.response.PopularDto
import com.phew.repository.TYPE_BLOCK
import com.phew.repository.TYPE_COMMENT_LIKE
import com.phew.repository.TYPE_COMMENT_WRITE
import com.phew.repository.TYPE_DELETE
import com.phew.repository.TYPE_FEED_LIKE
import com.phew.repository.TYPE_FOLLOW

internal fun NotificationDTO.toDomain(): Notification {
    return when (this.notificationType) {
        TYPE_FEED_LIKE -> {
            FeedLikeNotification(
                createTime = this.createTime,
                notificationId = this.notificationId,
                nickName = this.nickName ?: "error_nickName",
                targetCardId = this.targetCardId ?: -1,
                userId = this.userId ?: -1
            )
        }

        TYPE_FOLLOW -> {
            FollowNotification(
                notificationId = this.notificationId,
                createTime = this.createTime,
                nickName = this.nickName ?: "error_nickName",
                userId = this.userId ?: -1
            )
        }

        TYPE_COMMENT_LIKE -> {
            UserCommentLike(
                notificationId = this.notificationId,
                createTime = this.createTime,
                nickName = this.nickName ?: "error_nickname",
                targetCardId = this.targetCardId ?: -1,
                userId = this.userId ?: -1
            )
        }

        TYPE_COMMENT_WRITE -> {
            UserCommentWrite(
                notificationId = this.notificationId,
                createTime = this.createTime,
                targetCardId = this.targetCardId ?: -1,
                userId = this.userId ?: -1,
                nickName = nickName ?: "error_nickname"
            )
        }

        TYPE_BLOCK -> {
            UserBlockNotification(
                notificationId = this.notificationId,
                createTime = this.createTime,
                blockExpirationDateTime = this.blockExpirationDateTime ?: "error_block_time"
            )
        }

        TYPE_DELETE -> {
            UserDeleteNotification(
                notificationId = this.notificationId,
                createTime = this.createTime,
            )
        }

        else -> throw IllegalArgumentException("Unknown type")
    }
}

internal fun PopularDto.toDomain(): Popular {
    return Popular(
        cardId = this.cardId,
        likeCount = this.likeCount,
        commentCardCount = this.commentCardCount,
        cardImgUrl = this.cardImgUrl,
        cardImageName = this.cardImageName,
        cardContent = this.cardContent,
        font = this.font,
        distance = this.distance,
        createAt = this.createAt,
        storyExpirationTime = this.storyExpirationTime,
        isAdminCard = this.isAdminCard
    )
}

internal fun LatestDto.toDomain(): Latest {
    return Latest(
        cardId = this.cardId,
        likeCount = this.likeCount,
        commentCardCount = this.commentCardCount,
        cardImgUrl = this.cardImgUrl,
        cardImageName = this.cardImageName,
        cardContent = this.cardContent,
        font = this.font,
        distance = this.distance,
        createAt = this.createAt,
        storyExpirationTime = this.storyExpirationTime,
        isAdminCard = this.isAdminCard
    )
}