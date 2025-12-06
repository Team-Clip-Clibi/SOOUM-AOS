package com.phew.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class CheckCardDeleteDTO(
    val isDeleted: Boolean,
)
