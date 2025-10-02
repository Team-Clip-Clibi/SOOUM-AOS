package com.phew.network.dto

import kotlinx.serialization.Serializable

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class TokenDTO(
    val accessToken: String,
    val refreshToken: String,
)