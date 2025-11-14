package com.phew.domain.dto


data class FollowData(
    val followId: Long,
    val memberId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val isFollowing: Boolean,
    val isRequester: Boolean,
)