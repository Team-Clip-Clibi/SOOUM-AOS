package com.phew.domain.dto

data class CheckSignUp(
    val time: String,
    val banned: Boolean,
    val withdrawn: Boolean,
    val registered: Boolean,
)