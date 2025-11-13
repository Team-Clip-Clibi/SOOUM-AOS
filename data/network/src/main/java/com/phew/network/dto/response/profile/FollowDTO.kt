package com.phew.network.dto.response.profile

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class FollowDTO(
    @SerializedName("followerContent")
    val followerData: List<FollowDataDTO>,
)

@Serializable
data class FollowDataDTO(
    val memberId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val isFollowing: Boolean,
    val isRequester: Boolean,
)