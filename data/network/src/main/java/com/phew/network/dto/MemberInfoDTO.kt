package com.phew.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class MemberInfoDTO(
    val encryptedDeviceId: String,
    val deviceType: String = "ANDROID",
    val fcmToken: String,
    val isNotificationAgreed: Boolean,
    val nickname: String,
    val profileImage: String
)