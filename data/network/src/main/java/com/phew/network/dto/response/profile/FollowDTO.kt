package com.phew.network.dto.response.profile

import kotlinx.serialization.Serializable


@Serializable
data class FollowDataDTO(
    val followId: Long,
    val memberId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val isFollowing: Boolean,
    val isRequester: Boolean,
)