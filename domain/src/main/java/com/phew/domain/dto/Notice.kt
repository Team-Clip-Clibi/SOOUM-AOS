package com.phew.domain.dto

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Notice(
    val title: String,
    val url: String,
    val createdAt: String,
    val id : Int
) {
    val viewTime = createdAt.viewTime()

    private fun String.viewTime(): String {
        val parsed = LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        return parsed.format(DateTimeFormatter.ofPattern("M월 d일"))
    }
}