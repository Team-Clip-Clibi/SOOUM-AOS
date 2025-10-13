package com.phew.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class BackgroundImageDTO(
    val isAvailableImg: Boolean,
)