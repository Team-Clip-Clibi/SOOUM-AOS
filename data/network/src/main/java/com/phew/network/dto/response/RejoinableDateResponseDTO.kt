package com.phew.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class RejoinableDateResponseDTO(
    val rejoinableDate: String,
    val isActivityRestricted: Boolean
)