package com.phew.domain.repository.network

import com.phew.core_common.DataResult
import com.phew.domain.dto.FollowData
import com.phew.domain.dto.MyProfileInfo
import com.phew.domain.dto.ProfileCard

interface ProfileRepository {
    suspend fun requestMyProfile(): DataResult<MyProfileInfo>
    suspend fun requestProfileFeedCard(userId: Long): DataResult<Pair<Int, List<ProfileCard>>>
    suspend fun requestProfileFeedCardNext(
        userId: Long,
        cardId: Long,
    ): DataResult<Pair<Int, List<ProfileCard>>>

    suspend fun requestProfileCommentCard(): DataResult<Pair<Int, List<ProfileCard>>>
    suspend fun requestProfileCommentCardNext(cardId: Long): DataResult<Pair<Int, List<ProfileCard>>>
    suspend fun requestFollower(profileId : Long): DataResult<Pair<Int, List<FollowData>>>
    suspend fun requestFollowerNext(profileId : Long,lastId: Long): DataResult<Pair<Int, List<FollowData>>>
    suspend fun requestFollowing(profileId : Long): DataResult<Pair<Int, List<FollowData>>>
    suspend fun requestFollowingNext(profileId : Long,lastId: Long): DataResult<Pair<Int, List<FollowData>>>
}