package com.phew.domain.dto

data class MyProfileInfo(
    val userId: Int,
    val nickname: String,
    val profileImgName: String,
    val profileImageUrl: String,
    val totalVisitCnt: Int,
    val todayVisitCnt: Int,
    val cardCnt: Int,
    val followingCnt: Int,
    val followerCnt: Int,
)