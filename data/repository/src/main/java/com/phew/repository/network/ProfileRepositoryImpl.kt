package com.phew.repository.network

import com.phew.core_common.DataResult
import com.phew.domain.dto.FollowData
import com.phew.domain.dto.MyProfileInfo
import com.phew.domain.dto.ProfileCard
import com.phew.domain.repository.network.ProfileRepository
import com.phew.network.retrofit.ProfileHttp
import com.phew.repository.mapper.apiCall
import com.phew.repository.mapper.pagingCall
import com.phew.repository.mapper.toDomain
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(private val http: ProfileHttp) :
    ProfileRepository {
    override suspend fun requestMyProfile(): DataResult<MyProfileInfo> {
        return apiCall(
            apiCall = { http.requestMyProfile() },
            mapper = { data -> data.toDomain() }
        )
    }

    override suspend fun requestProfileFeedCard(userId: Long): DataResult<Pair<Int, List<ProfileCard>>> {
        return pagingCall(
            apiCall = { http.requestMyProfileFeedCard(userId = userId) },
            mapper = { data -> data.cardContents.map { cardContentDto -> cardContentDto.toDomain() } }
        )
    }

    override suspend fun requestProfileFeedCardNext(
        userId: Long,
        cardId: Long,
    ): DataResult<Pair<Int, List<ProfileCard>>> {
        return pagingCall(
            apiCall = { http.requestMyProfileFeedCardNext(userId = userId, lastId = cardId) },
            mapper = { data -> data.cardContents.map { cardContentDto -> cardContentDto.toDomain() } }
        )
    }

    override suspend fun requestProfileCommentCard(): DataResult<Pair<Int, List<ProfileCard>>> {
        return pagingCall(
            apiCall = { http.requestMyProfileCommentCard() },
            mapper = { data -> data.cardContents.map { cardContentDto -> cardContentDto.toDomain() } }
        )
    }

    override suspend fun requestProfileCommentCardNext(cardId: Long): DataResult<Pair<Int, List<ProfileCard>>> {
        return pagingCall(
            apiCall = { http.requestMyProfileCommentCardNext(lastId = cardId) },
            mapper = { data -> data.cardContents.map { cardContentDto -> cardContentDto.toDomain() } }
        )
    }

    override suspend fun requestFollower(profileId: Long): DataResult<Pair<Int, List<FollowData>>> {
        return pagingCall(
            apiCall = { http.requestFollower(profileOwnerId = profileId) },
            mapper = { data -> data.followerData.map { followData -> followData.toDomain() } }
        )
    }

    override suspend fun requestFollowerNext(
        profileId: Long,
        lastId: Long,
    ): DataResult<Pair<Int, List<FollowData>>> {
        return pagingCall(
            apiCall = { http.requestFollowerNext(profileOwnerId = profileId, lastId = lastId) },
            mapper = { data -> data.followerData.map { followData -> followData.toDomain() } }
        )
    }

    override suspend fun requestFollowing(
        profileId: Long,
    ): DataResult<Pair<Int, List<FollowData>>> {
        return pagingCall(
            apiCall = { http.requestFollowing(profileOwnerId = profileId) },
            mapper = { data -> data.followerData.map { followerData -> followerData.toDomain() } }
        )
    }

    override suspend fun requestFollowingNext(
        profileId: Long,
        lastId: Long,
    ): DataResult<Pair<Int, List<FollowData>>> {
        return pagingCall(
            apiCall = { http.requestFollowingNext(profileOwnerId = profileId, lastId = lastId) },
            mapper = { data -> data.followerData.map { followerData -> followerData.toDomain() } }
        )
    }
}