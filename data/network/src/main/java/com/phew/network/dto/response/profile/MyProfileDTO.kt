package com.phew.network.dto.response.profile

import kotlinx.serialization.Serializable

@Serializable
data class MyProfileDTO(
    val userId: Int,
    val nickname: String,
    val profileImgName: String,
    val profileImageUrl: String?,
    val totalVisitCnt: Int,
    val todayVisitCnt: Int,
    val cardCnt: Int,
    val followingCnt: Int,
    val followerCnt: Int,
)
