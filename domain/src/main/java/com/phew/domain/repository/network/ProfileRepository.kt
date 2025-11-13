package com.phew.domain.repository.network

import com.phew.core_common.DataResult
import com.phew.domain.dto.FollowData
import com.phew.domain.dto.ProfileInfo
import com.phew.domain.dto.ProfileCard
import com.phew.domain.dto.UploadImageUrl
import okhttp3.RequestBody

interface ProfileRepository {
    suspend fun requestMyProfile(): DataResult<ProfileInfo>
    suspend fun requestOtherProfile(profileId: Long): DataResult<ProfileInfo>
    suspend fun requestProfileFeedCard(userId: Long): DataResult<Pair<Int, List<ProfileCard>>>
    suspend fun requestProfileFeedCardNext(
        userId: Long,
        cardId: Long,
    ): DataResult<Pair<Int, List<ProfileCard>>>

    suspend fun requestProfileCommentCard(): DataResult<Pair<Int, List<ProfileCard>>>
    suspend fun requestProfileCommentCardNext(cardId: Long): DataResult<Pair<Int, List<ProfileCard>>>
    suspend fun requestFollower(profileId: Long): DataResult<Pair<Int, List<FollowData>>>
    suspend fun requestFollowerNext(
        profileId: Long,
        lastId: Long,
    ): DataResult<Pair<Int, List<FollowData>>>

    suspend fun requestFollowing(profileId: Long): DataResult<Pair<Int, List<FollowData>>>
    suspend fun requestFollowingNext(
        profileId: Long,
        lastId: Long,
    ): DataResult<Pair<Int, List<FollowData>>>

    suspend fun requestFollowUser(profileId: Long): DataResult<Boolean>
    suspend fun requestUnFollowUser(profileId: Long): DataResult<Boolean>
    suspend fun requestBlockUser(profileId: Long): DataResult<Boolean>
    suspend fun requestUnBlockUser(profileId: Long): DataResult<Boolean>
    suspend fun requestUploadImageUrl(): DataResult<UploadImageUrl>
    suspend fun requestUploadImage(uri: String, body: RequestBody): DataResult<Unit>
    suspend fun requestUpdateProfile(nickName: String?, profileImageName: String): DataResult<Unit>
}