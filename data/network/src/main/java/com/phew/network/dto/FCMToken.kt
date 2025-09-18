package com.phew.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class FCMToken(
    val fcmToken: String
)