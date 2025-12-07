package com.phew.sign_up.dto

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class SignUpResult (
    val time : String,
    val result : String
){
    val reFormTime: String
        get() {
            return try {
                val parsedTime = LocalDateTime.parse(time)
                val adjustedTime = parsedTime.plusDays(1)
                val outputFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
                outputFormatter.format(adjustedTime)
            } catch (e: DateTimeParseException) {
                time
            }
        }
}
