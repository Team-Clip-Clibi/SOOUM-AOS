package com.phew.network.dto.request.feed

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable


@OptIn(InternalSerializationApi::class)
@Serializable
data class CardFeedDto(
    val latitude: Double?,
    val longitude: Double?,
    val lastId: Int?
)
