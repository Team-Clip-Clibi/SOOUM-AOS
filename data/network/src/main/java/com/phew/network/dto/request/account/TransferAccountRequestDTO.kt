package com.phew.network.dto.request.account

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class TransferAccountRequestDTO(
    @SerializedName("transferCode")
    val transferCode: String,
    @SerializedName("encryptedDeviceId")
    val encryptedDeviceId: String,
    @SerializedName("deviceType")
    val deviceType: String,
    @SerializedName("deviceModel")
    val deviceModel: String,
    @SerializedName("deviceOsVersion")
    val deviceOsVersion: String
)