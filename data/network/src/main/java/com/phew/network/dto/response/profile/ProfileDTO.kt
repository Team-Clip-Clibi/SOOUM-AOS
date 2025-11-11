package com.phew.network.dto.response.profile

import kotlinx.serialization.Serializable

@Serializable
data class ProfileDTO(
    val userId: Long,
    val nickname: String,
    val profileImgName: String?,
    val profileImageUrl: String?,
    val totalVisitCnt: Int,
    val todayVisitCnt: Int,
    val cardCnt: Int,
    val followingCnt: Int,
    val followerCnt: Int,
    val isBlocked: Boolean = false,
    val isAlreadyFollowing: Boolean = false,
)