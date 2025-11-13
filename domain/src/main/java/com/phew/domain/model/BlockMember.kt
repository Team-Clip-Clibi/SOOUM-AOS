package com.phew.domain.model

data class BlockMember(
    val blockId: Long,
    val blockMemberId: Long,
    val blockMemberNickname: String,
    val blockMemberProfileImageUrl: String?
)