package com.phew.network.dto

import kotlinx.serialization.Serializable

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class NoticeDto(
    val notices: List<NoticeData>,
)

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
class NoticeData(
    val title: String,
    val url: String?,
    val createdAt: String,
    val id: Int,
)