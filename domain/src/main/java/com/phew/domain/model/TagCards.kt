package com.phew.domain.model

data class TagCards(
    val cardContents: List<CardContent>,
    val isFavorite: Boolean
)

data class CardContent(
    val cardId: Long,
    val cardImgName: String,
    val cardImgUrl: String,
    val cardContent: String,
    val font: String
)