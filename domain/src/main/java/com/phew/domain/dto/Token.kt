package com.phew.domain.dto

data class Token(
    val accessToken: String,
    val refreshToken: String
)