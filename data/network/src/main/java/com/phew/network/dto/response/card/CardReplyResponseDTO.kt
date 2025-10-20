package com.phew.network.dto.response.card

import kotlinx.serialization.Serializable

@Serializable
data class CardReplyResponseDTO(
    val isDistanceShared: Boolean,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val content: String,
    val font: String,
    val imgType: String,
    val imgName: String,
    val tags: List<String>
)
