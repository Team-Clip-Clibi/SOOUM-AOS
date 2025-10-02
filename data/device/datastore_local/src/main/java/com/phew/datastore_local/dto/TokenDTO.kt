package com.phew.datastore_local.dto

data class TokenDTO(
    val refreshToken: String,
    val accessToken: String,
)