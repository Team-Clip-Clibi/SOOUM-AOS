package com.phew.domain.dto

data class CardArticle(
    val cardId: Long,
    val profileImgUrl: String,
    val nickName: String,
    val cardContent: String,
    val isRead: Boolean,
    val writerProfileImageUrls: List<String>,
    val totalWriterCnt: Int,
)