package com.phew.network.dto.response

import kotlinx.serialization.Serializable

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class LatestDto(
    val cardId: String,
    val likeCount: Int,
    val commentCardCount: Int,
    val cardImgUrl: String,
    val cardImagName: String,
    val cardContent: String,
    val font: String,
    val distance: String,
    val createAt: String,
    val storyExpirationTime: String,
    val isAdminCard: Boolean
)
