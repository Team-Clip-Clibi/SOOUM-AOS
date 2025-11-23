package com.phew.network.dto.request.feed

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TagInfoListDTO(
    @SerialName("tagInfos")
    val tagInfo: List<TagInfoDTO>
)

@Serializable
data class TagInfoDTO(
    val id: Long,
    val name: String,
    val usageCnt: Int
)