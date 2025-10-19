package com.phew.domain.dto
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
        val parsed = LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        return parsed.format(DateTimeFormatter.ofPattern("M월 d일"))
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