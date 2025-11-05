package com.phew.network.dto.response.card

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileCardDTO(
    @SerializedName("cardContents")
    val cardContents: List<CardContentDto>,
)
@Serializable
data class CardContentDto(
    val cardId: Long,
    val cardImgName: String?,
    val cardImgUrl: String?,
    val cardContent: String?,
)