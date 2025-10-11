package com.phew.network.dto.request.feed

data class RequestUploadCardAnswerDTO (
    val isDistanceShared: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val content: String,
    val font: String,
    val imgType: String,
    val imgName: String,
    val tags: List<String>
)