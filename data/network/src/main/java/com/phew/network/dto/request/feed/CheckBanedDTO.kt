package com.phew.network.dto.request.feed

import kotlinx.serialization.Serializable

@Serializable
data class CheckBanedDTO(
    val isBaned: Boolean,
    val expiredAt: String?
)