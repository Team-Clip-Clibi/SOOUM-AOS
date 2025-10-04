package com.phew.domain.dto

sealed class FeedCardType {
    data class BoombType(
        val cardId: String,
        val storyExpirationTime: String,
        val content: String,
        val imageUrl: String,
        val imageName: String,
        val font: String,
        val location: String,
        val writeTime: String,
        val commentValue: String,
        val likeValue: String
    ) : FeedCardType()
    
    data class AdminType(
        val cardId: String,
        val content: String,
        val imageUrl: String,
        val imageName: String,
        val font: String,
        val location: String,
        val writeTime: String,
        val commentValue: String,
        val likeValue: String
    ) : FeedCardType()
    
    data class NormalType(
        val cardId: String,
        val content: String,
        val imageUrl: String,
        val imageName: String,
        val font: String,
        val location: String,
        val writeTime: String,
        val commentValue: String,
        val likeValue: String
    ) : FeedCardType()
}