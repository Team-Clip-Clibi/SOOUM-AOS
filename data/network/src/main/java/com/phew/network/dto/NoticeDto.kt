package com.phew.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class NoticeDto(
    val notices: List<NoticeData>,
)

@Serializable
data class NoticeData(
    val title: String,
    val url: String,
    val createdAt: String,
    val id: Int,
)