package com.phew.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class UserRoleResponseDTO(
    val role: UserRoleDTO,
    val isTester: Boolean
)

@Serializable
enum class UserRoleDTO {
    ADMIN,
    USER,
    BANNED
}
