package com.phew.domain.dto

data class Latest(
    val cardId: String,
    val likeCount: Int,
    val commentCardCount: Int,
    val cardImgUrl: String,
    val cardImageName: String,
    val cardContent: String,
    val font: String,
    val distance: String?,
    val createAt: String,
    val storyExpirationTime: String?,
    val isAdminCard: Boolean
)