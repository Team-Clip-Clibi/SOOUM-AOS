package com.phew.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class AppVersionStatusDTO(
    val status: AppVersionStatus,
    val latestVersion: String
)