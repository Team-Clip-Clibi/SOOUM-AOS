package com.phew.domain.repository.network

import com.phew.domain.dto.FollowData
import com.phew.domain.dto.ProfileInfo
import com.phew.domain.dto.ProfileCard
import com.phew.domain.dto.UploadImageUrl
import okhttp3.RequestBody

interface ProfileRepository {
    suspend fun requestMyProfile(): Result<ProfileInfo>
    suspend fun requestOtherProfile(profileId: Long): Result<ProfileInfo>
    suspend fun requestProfileFeedCard(userId: Long): Result<Pair<Int, List<ProfileCard>>>
    suspend fun requestProfileFeedCardNext(
        userId: Long,
        cardId: Long,
    ): Result<Pair<Int, List<ProfileCard>>>

    suspend fun requestProfileCommentCard(): Result<Pair<Int, List<ProfileCard>>>
    suspend fun requestProfileCommentCardNext(cardId: Long): Result<Pair<Int, List<ProfileCard>>>
    suspend fun requestFollower(profileId: Long): Result<Pair<Int, List<FollowData>>>
    suspend fun requestFollowerNext(
        profileId: Long,
        lastId: Long,
    ): Result<Pair<Int, List<FollowData>>>

    suspend fun requestFollowing(profileId: Long): Result<Pair<Int, List<FollowData>>>
    suspend fun requestFollowingNext(
        profileId: Long,
        lastId: Long,
    ): Result<Pair<Int, List<FollowData>>>

    suspend fun requestFollowUser(profileId: Long): Result<Boolean>
    suspend fun requestUnFollowUser(profileId: Long): Result<Boolean>
    suspend fun requestBlockUser(profileId: Long): Result<Boolean>
    suspend fun requestUnBlockUser(profileId: Long): Result<Boolean>
    suspend fun requestUploadImageUrl(): Result<UploadImageUrl>
    suspend fun requestUploadImage(uri: String, body: RequestBody): Result<Unit>
    suspend fun requestUpdateProfile(
        nickName: String?,
        profileImageName: String?,
        profileBio: String?,
    ): Result<Unit>
}
