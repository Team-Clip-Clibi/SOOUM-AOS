package com.phew.network.dto

import kotlinx.serialization.Serializable

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class SignUpRequest(
    val memberInfo: MemberInfoDTO,
    val policy: PolicyDTO
)