package com.phew.network.dto.request.feed

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DefaultImageDTO(
    @SerialName("defaultImages")
    val defaultImages: Map<String, List<ImageInfoDTO>>
)

@Serializable
data class ImageInfoDTO(
    val imgName: String,
    val url: String
)