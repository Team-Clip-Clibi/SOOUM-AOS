package com.phew.domain.dto
import com.phew.core_common.TimeUtils

data class Notice(
    val content: String,
    val url: String,
    val createdAt: String,
    val noticeType: String,
    val id: Int,
) {
    val viewTime = createdAt.viewTime()
    val type = noticeType.viewNoticeType()

    private fun String.viewTime(): String {
        return TimeUtils.formatToSimpleDate(this)
    }

    private fun String.viewNoticeType(): NoticeType {
        try {
            return NoticeType.valueOf(this.uppercase())
        } catch (e: Exception) {
            e.printStackTrace()
            return NoticeType.ANNOUNCEMENT
        }
    }

    enum class NoticeType {
        ANNOUNCEMENT,
        NEWS,
        MAINTENANCE;
    }
}