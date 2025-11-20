package com.phew.domain.dto

data class TagCardContent(
    val cardId: Long,
    val cardImgName: String,
    val cardImgUrl: String,
    val cardContent: String,
    val font: String,
    val isFavorite: Boolean = false
)