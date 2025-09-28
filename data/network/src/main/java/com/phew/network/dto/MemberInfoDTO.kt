package com.phew.network.dto

import kotlinx.serialization.Serializable

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class MemberInfoDTO(
    val encryptedDeviceId: String,
    val deviceType: String,
    val fcmToken: String,
    val isNotificationAgreed: Boolean,
    val nickname: String,
    val profileImage: String?
)