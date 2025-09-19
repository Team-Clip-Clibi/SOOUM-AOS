package com.phew.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class InfoDTO(
    val encryptedDeviceId: String,
)