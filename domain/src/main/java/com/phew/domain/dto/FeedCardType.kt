package com.phew.domain.dto

import com.phew.core_common.CheckEventCard

sealed class FeedCardType {
    data class BoombType(
        val cardId: String,
        val storyExpirationTime: String?,
        val content: String,
        val imageUrl: String,
        val imageName: String,
        val font: String,
        val location: String?,
        val writeTime: String,
        val commentValue: String,
        val likeValue: String,
    ) : FeedCardType()

    data class AdminType(
        val cardId: String,
        val content: String,
        val imageUrl: String,
        val imageName: String,
        val font: String,
        val location: String?,
        val writeTime: String,
        val commentValue: String,
        val likeValue: String,
    ) : FeedCardType()

    data class NormalType(
        val cardId: String,
        val content: String,
        val imageUrl: String,
        val imageName: String,
        val font: String,
        val location: String?,
        val writeTime: String,
        val commentValue: String,
        val likeValue: String,
    ) : FeedCardType()

    fun isEventCard(): Boolean {
        val targetImageName = when (this) {
            is BoombType -> this.imageName
            is AdminType -> this.imageName
            is NormalType -> this.imageName
        }
        return with(CheckEventCard) { targetImageName.isEventCard() }
    }
}