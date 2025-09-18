package com.phew.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(
    val memberInfo: MemberInfoDTO,
    val policy: PolicyDTO
)