package com.phew.network.dto

import kotlinx.serialization.Serializable

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class UploadImageUrlDTO(
    val imgName: String,
    val imgUrl: String
)