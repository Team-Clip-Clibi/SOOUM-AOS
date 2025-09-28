package com.phew.network.dto

import kotlinx.serialization.Serializable

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class NotificationDTO(
    val notificationId: Long,
    val notificationType: String,
    val createTime: String,

    val blockExpirationDateTime: String? = null,
    val nickName: String? = null,
    val userId: Long? = null,
    val targetCardId: Int? = null
)