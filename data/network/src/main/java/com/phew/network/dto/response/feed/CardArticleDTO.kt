package com.phew.network.dto.response.feed

import kotlinx.serialization.Serializable

@Serializable
data class CardArticleDTO(
    val cardId: Long,
    val abTestType: String,
    val profileImgUrl: String,
    val nickname: String,
    val cardContent: String,
    val isRead: Boolean,
    val writerProfileImgUrls: List<String>? = null,
    val totalWriterCnt: Int? = null,
)