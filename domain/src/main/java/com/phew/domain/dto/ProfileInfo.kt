package com.phew.domain.dto

data class ProfileInfo(
    val userId: Long,
    val nickname: String,
    val profileImgName: String,
    val profileImageUrl: String,
    val totalVisitCnt: Int,
    val todayVisitCnt: Int,
    val cardCnt: Int,
    val followingCnt: Int,
    val followerCnt: Int,
    val isBlocked: Boolean,
    val isAlreadyFollowing: Boolean,
)