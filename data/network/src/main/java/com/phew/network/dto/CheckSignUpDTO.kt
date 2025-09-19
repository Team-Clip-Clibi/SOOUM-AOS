package com.phew.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CheckSignUpDTO(
    val rejoinAvailableAt: String?,
    val banned: Boolean,
    val withdrawn: Boolean,
    val registered: Boolean,
)