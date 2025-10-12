package com.phew.domain.dto

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

data class CheckedBaned(
    val isBaned: Boolean,
    val expiredAt: String?
) {
    val viewTime = expiredAt.toViewTime()

    private fun String?.toViewTime(): String {
        if (this == null) return ""
        val dateTime = OffsetDateTime.parse(this)
        val formatter = DateTimeFormatter.ofPattern("yyyy년MM월dd일")
        val formattedDate = dateTime.format(formatter)
        return formattedDate
    }
}