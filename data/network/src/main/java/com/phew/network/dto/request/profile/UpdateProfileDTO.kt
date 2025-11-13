package com.phew.network.dto.request.profile

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileDTO(
    val nickName: String?,
    val profileImgName: String,
)