package com.phew.network.dto

import kotlinx.serialization.Serializable

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class InfoDTO(
    val encryptedDeviceId: String,
    val deviceType: String,
    val deviceOsVersion: String,
    val deviceModel: String
)