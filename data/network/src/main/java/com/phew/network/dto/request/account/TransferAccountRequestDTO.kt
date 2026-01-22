package com.phew.network.dto.request.account

import kotlinx.serialization.Serializable

@Serializable
data class TransferAccountRequestDTO(
    val transferCode: String,
    val encryptedDeviceId: String,
    val deviceType: String,
    val deviceModel: String,
    val deviceOsVersion: String
)