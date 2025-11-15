package com.phew.network.dto.request.account

import kotlinx.serialization.Serializable

@Serializable
data class WithdrawalRequestDTO(
    val accessToken: String,
    val refreshToken: String,
    val reason: String
)