package com.phew.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class UploadImageUrlDTO(
    val imgName: String,
    val imgUrl: String
)