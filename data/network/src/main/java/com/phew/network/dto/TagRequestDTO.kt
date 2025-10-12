package com.phew.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class TagRequestDTO(
    val tag: String
)