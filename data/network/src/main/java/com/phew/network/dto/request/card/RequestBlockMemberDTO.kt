package com.phew.network.dto.request.card

import kotlinx.serialization.Serializable

@Serializable
data class RequestBlockMemberDTO(
    val toMemberId: Long
)