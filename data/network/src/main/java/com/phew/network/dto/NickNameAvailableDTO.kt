package com.phew.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class NickNameAvailableDTO(
    val isAvailable: Boolean,
)