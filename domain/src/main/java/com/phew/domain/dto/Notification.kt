package com.phew.domain.dto

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

sealed class Notification {
    abstract val notificationId: Long
    abstract val createTime: String
}

/**
 * 좋아요
 */
data class FeedLikeNotification(
    override val notificationId: Long,
    override val createTime: String,
    val nickName: String,
    val targetCardId: Int,
    val userId: Long
) : Notification() {
    val viewTime = createTime.toRelativeTime()

    private fun String.toRelativeTime(): String {
        val pastInstant: Instant = try {
            val localDateTime = LocalDateTime.parse(this)
            localDateTime.toInstant(ZoneOffset.UTC)
        } catch (e: Exception) {
            e.printStackTrace()
            return "날짜 오류"
        }
        val nowInstant: Instant = Instant.now()
        val duration: Duration = Duration.between(pastInstant, nowInstant)
        val totalMinutes = duration.toMinutes()
        val totalHours = duration.toHours()
        val totalDays = duration.toDays()
        val pastDateTime = pastInstant.atZone(ZoneId.systemDefault())

        return when {
            totalMinutes < 10 -> "방금 전"
            totalMinutes < 60 -> "${totalMinutes}분 전"
            totalHours < 24 -> "${totalHours}시간 전"
            totalDays < 30 -> "${totalDays}일 전"
            totalDays < 365 -> {
                val formatter = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREA)
                pastDateTime.format(formatter)
            }

            else -> {
                val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREA)
                pastDateTime.format(formatter)
            }
        }
    }
}

/**
 * 팔로우
 */
data class FollowNotification(
    override val notificationId: Long,
    override val createTime: String,
    val nickName: String,
    val userId: Long
) : Notification() {
    val viewTime = createTime.toRelativeTime()

    private fun String.toRelativeTime(): String {
        val pastInstant: Instant = try {
            val localDateTime = LocalDateTime.parse(this)
            localDateTime.toInstant(ZoneOffset.UTC)
        } catch (e: Exception) {
            e.printStackTrace()
            return "날짜 오류"
        }
        val nowInstant: Instant = Instant.now()
        val duration: Duration = Duration.between(pastInstant, nowInstant)
        val totalMinutes = duration.toMinutes()
        val totalHours = duration.toHours()
        val totalDays = duration.toDays()
        val pastDateTime = pastInstant.atZone(ZoneId.systemDefault())

        return when {
            totalMinutes < 10 -> "방금 전"
            totalMinutes < 60 -> "${totalMinutes}분 전"
            totalHours < 24 -> "${totalHours}시간 전"
            totalDays < 30 -> "${totalDays}일 전"
            totalDays < 365 -> {
                val formatter = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREA)
                pastDateTime.format(formatter)
            }

            else -> {
                val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREA)
                pastDateTime.format(formatter)
            }
        }
    }
}

/**
 * 차단
 */
data class UserBlockNotification(
    override val notificationId: Long,
    override val createTime: String,
    val blockExpirationDateTime: String
) : Notification() {
    val viewTime = createTime.toRelativeTime()
    val blockTimeView = blockExpirationDateTime.toBlockTime()

    private fun String.toBlockTime(): String {
        val pastInstant: Instant = try {
            val localDateTime = LocalDateTime.parse(this)
            localDateTime.toInstant(ZoneOffset.UTC)
        } catch (e: Exception) {
            e.printStackTrace()
            return "날짜 오류"
        }
        val pastDateTime = pastInstant.atZone(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREA)
        return pastDateTime.format(formatter)
    }

    private fun String.toRelativeTime(): String {
        val pastInstant: Instant = try {
            val localDateTime = LocalDateTime.parse(this)
            localDateTime.toInstant(ZoneOffset.UTC)
        } catch (e: Exception) {
            e.printStackTrace()
            return "날짜 오류"
        }
        val nowInstant: Instant = Instant.now()
        val duration: Duration = Duration.between(pastInstant, nowInstant)
        val totalMinutes = duration.toMinutes()
        val totalHours = duration.toHours()
        val totalDays = duration.toDays()
        val pastDateTime = pastInstant.atZone(ZoneId.systemDefault())

        return when {
            totalMinutes < 10 -> "방금 전"
            totalMinutes < 60 -> "${totalMinutes}분 전"
            totalHours < 24 -> "${totalHours}시간 전"
            totalDays < 30 -> "${totalDays}일 전"
            totalDays < 365 -> {
                val formatter = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREA)
                pastDateTime.format(formatter)
            }

            else -> {
                val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREA)
                pastDateTime.format(formatter)
            }
        }
    }
}

/**
 * 삭제
 */
data class UserDeleteNotification(
    override val notificationId: Long,
    override val createTime: String,
) : Notification() {
    val viewTime = createTime.toViewTime()

    private fun String.toViewTime(): String {
        val pastInstant: Instant = try {
            val localDateTime = LocalDateTime.parse(this)
            localDateTime.toInstant(ZoneOffset.UTC)
        } catch (e: Exception) {
            e.printStackTrace()
            return "날짜 오류"
        }
        val nowInstant: Instant = Instant.now()
        val duration: Duration = Duration.between(pastInstant, nowInstant)
        val totalMinutes = duration.toMinutes()
        val totalHours = duration.toHours()
        val totalDays = duration.toDays()
        val pastDateTime = pastInstant.atZone(ZoneId.systemDefault())

        return when {
            totalMinutes < 10 -> "방금 전"
            totalMinutes < 60 -> "${totalMinutes}분 전"
            totalHours < 24 -> "${totalHours}시간 전"
            totalDays < 30 -> "${totalDays}일 전"
            totalDays < 365 -> {
                val formatter = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREA)
                pastDateTime.format(formatter)
            }

            else -> {
                val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREA)
                pastDateTime.format(formatter)
            }
        }
    }
}

/**
 * 댓글 좋아요
 */
data class UserCommentLike(
    override val notificationId: Long,
    override val createTime: String,
    val nickName: String,
    val targetCardId: Int,
    val userId: Long
) : Notification() {
    val viewTime = createTime.toRelativeTime()

    private fun String.toRelativeTime(): String {
        val pastInstant: Instant = try {
            val localDateTime = LocalDateTime.parse(this)
            localDateTime.toInstant(ZoneOffset.UTC)
        } catch (e: Exception) {
            e.printStackTrace()
            return "날짜 오류"
        }
        val nowInstant: Instant = Instant.now()
        val duration: Duration = Duration.between(pastInstant, nowInstant)
        val totalMinutes = duration.toMinutes()
        val totalHours = duration.toHours()
        val totalDays = duration.toDays()
        val pastDateTime = pastInstant.atZone(ZoneId.systemDefault())

        return when {
            totalMinutes < 10 -> "방금 전"
            totalMinutes < 60 -> "${totalMinutes}분 전"
            totalHours < 24 -> "${totalHours}시간 전"
            totalDays < 30 -> "${totalDays}일 전"
            totalDays < 365 -> {
                val formatter = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREA)
                pastDateTime.format(formatter)
            }

            else -> {
                val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREA)
                pastDateTime.format(formatter)
            }
        }
    }
}

/**
 * 댓글 작성
 */
data class UserCommentWrite(
    override val notificationId: Long,
    override val createTime: String,
    val targetCardId: Int,
    val userId: Long,
    val nickName: String,
) : Notification() {
    val viewTime = createTime.toRelativeTime()

    private fun String.toRelativeTime(): String {
        val pastInstant: Instant = try {
            val localDateTime = LocalDateTime.parse(this)
            localDateTime.toInstant(ZoneOffset.UTC)
        } catch (e: Exception) {
            e.printStackTrace()
            return "날짜 오류"
        }
        val nowInstant: Instant = Instant.now()
        val duration: Duration = Duration.between(pastInstant, nowInstant)
        val totalMinutes = duration.toMinutes()
        val totalHours = duration.toHours()
        val totalDays = duration.toDays()
        val pastDateTime = pastInstant.atZone(ZoneId.systemDefault())

        return when {
            totalMinutes < 10 -> "방금 전"
            totalMinutes < 60 -> "${totalMinutes}분 전"
            totalHours < 24 -> "${totalHours}시간 전"
            totalDays < 30 -> "${totalDays}일 전"
            totalDays < 365 -> {
                val formatter = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREA)
                pastDateTime.format(formatter)
            }

            else -> {
                val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREA)
                pastDateTime.format(formatter)
            }
        }
    }
}