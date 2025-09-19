package com.phew.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class SecurityKeyDTO(
    val publicKey: String
)