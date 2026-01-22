package com.phew.network.dto.response.feed

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class CardIdResponseDto(
    @SerializedName("cardId")
    val cardId: Long
)
