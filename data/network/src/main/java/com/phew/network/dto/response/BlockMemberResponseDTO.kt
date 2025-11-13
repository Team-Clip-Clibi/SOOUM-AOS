package com.phew.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class BlockMemberResponseDTO(
    val blockId: Long,
    val blockMemberId: Long,
    val blockMemberNickname: String,
    val blockMemberProfileImageUrl: String?
)