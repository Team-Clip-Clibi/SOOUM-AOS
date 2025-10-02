package com.phew.network.dto

import kotlinx.serialization.Serializable

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class AppVersionDTO(
    val status: String,
)