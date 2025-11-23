package com.phew.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class TagCardsResponseDTO(
    val cardContents: List<CardContentDTO>,
    val isFavorite: Boolean
)

@Serializable
data class CardContentDTO(
    val cardId: Long,
    val cardImgName: String,
    val cardImgUrl: String,
    val cardContent: String,
    val font: String
)