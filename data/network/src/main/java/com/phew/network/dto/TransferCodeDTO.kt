package com.phew.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class TransferCodeDTO(
    val transferCode: String,
    val expiredAt: String
)