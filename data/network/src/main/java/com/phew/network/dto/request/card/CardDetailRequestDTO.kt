package com.phew.network.dto.request.card

import kotlinx.serialization.Serializable

@Serializable
data class CardDetailRequestDTO(
    val cardId: Int,
    val latitude: Double? = null,
    val longitude: Double? = null
)
