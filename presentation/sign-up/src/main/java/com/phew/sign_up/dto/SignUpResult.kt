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
                val outputFormatter = DateTimeFormatter.ofPattern("YYYY년 MM월 dd일")
                outputFormatter.format(parsedTime)
            } catch (e: DateTimeParseException) {
                time
            }
        }
}