package com.phew.domain.dto

sealed interface CardArticle {
    val cardId: Long
    val profileImgUrl: String
    val nickName: String
    val cardContent: String
    val isRead: Boolean

    data class TypeA(
        override val cardId: Long,
        override val profileImgUrl: String,
        override val nickName: String,
        override val cardContent: String,
        override val isRead: Boolean,
    ) : CardArticle

    data class TypeB(
        override val cardId: Long,
        override val profileImgUrl: String,
        override val nickName: String,
        override val cardContent: String,
        override val isRead: Boolean,
        val writerProfileImageUrls: List<String>,
        val totalWriterCnt: Int,
    ) : CardArticle
}