package com.phew.network.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class LatestDto(
    @SerialName("cardId")
    val cardId: String,
    @SerialName("likeCnt")
    val likeCount: Int,
    @SerialName("commentCardCnt")
    val commentCardCount: Int,
    @SerialName("cardImgUrl")
    val cardImgUrl: String,
    @SerialName("cardImgName")
    val cardImageName: String,
    @SerialName("cardContent")
    val cardContent: String,
    @SerialName("font")
    val font: String,
    @SerialName("distance")
    val distance: String?,
    @SerialName("createdAt")
    val createAt: String,
    @SerialName("storyExpirationTime")
    val storyExpirationTime: String?,
    @SerialName("isAdminCard")
    val isAdminCard: Boolean
)
