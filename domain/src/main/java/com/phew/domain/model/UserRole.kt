package com.phew.domain.model

data class UserRole(
    val role: UserRoleType,
    val isTester: Boolean
)

enum class UserRoleType {
    ADMIN,
    USER,
    BANNED
}
