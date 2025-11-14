package com.phew.network.dto.request.profile

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileDTO(
    val nickname: String?,
    val profileImgName: String?,
)