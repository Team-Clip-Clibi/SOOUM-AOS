package com.phew.network.dto.request.feed

import kotlinx.serialization.Serializable

@Serializable
data class RequestUploadCardDTO(
    val isDistanceShared: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val content: String,
    val font: String,
    val imgType: String,
    val imgName: String,
    val isStory: Boolean,
    val tags: List<String>
)