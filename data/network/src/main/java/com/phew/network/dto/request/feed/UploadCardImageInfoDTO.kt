package com.phew.network.dto.request.feed

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadCardImageInfoDTO(
    @SerialName("imgName")
    val imgName: String,
    @SerialName("imgUrl")
    val url: String
)
