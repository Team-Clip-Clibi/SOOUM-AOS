package com.phew.repository.network

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.DataResult
import com.phew.domain.dto.FollowData
import com.phew.domain.dto.ProfileInfo
import com.phew.domain.dto.ProfileCard
import com.phew.domain.repository.network.ProfileRepository
import com.phew.network.retrofit.ProfileHttp
import com.phew.repository.mapper.apiCall
import com.phew.repository.mapper.pagingCall
import com.phew.repository.mapper.toDomain
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(private val http: ProfileHttp) :
    ProfileRepository {
    override suspend fun requestMyProfile(): DataResult<ProfileInfo> {
        return apiCall(
            apiCall = { http.requestMyProfile() },
            mapper = { data -> data.toDomain() }
        )
    }

    override suspend fun requestOtherProfile(profileId: Long): DataResult<ProfileInfo> {
        return apiCall(
            apiCall = { http.requestOtherProfile(profileOwnerId = profileId) },
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
            mapper = { data -> data.map { followData -> followData.toDomain() } }
        )
    }

    override suspend fun requestFollowerNext(
        profileId: Long,
        lastId: Long,
    ): DataResult<Pair<Int, List<FollowData>>> {
        return pagingCall(
            apiCall = { http.requestFollowerNext(profileOwnerId = profileId, lastId = lastId) },
            mapper = { data -> data.map { followData -> followData.toDomain() } }
        )
    }

    override suspend fun requestFollowing(
        profileId: Long,
    ): DataResult<Pair<Int, List<FollowData>>> {
        return pagingCall(
            apiCall = { http.requestFollowing(profileOwnerId = profileId) },
            mapper = { data -> data.map { followerData -> followerData.toDomain() } }
        )
    }

    override suspend fun requestFollowingNext(
        profileId: Long,
        lastId: Long,
    ): DataResult<Pair<Int, List<FollowData>>> {
        return pagingCall(
            apiCall = { http.requestFollowingNext(profileOwnerId = profileId, lastId = lastId) },
            mapper = { data -> data.map { followerData -> followerData.toDomain() } }
        )
    }

    override suspend fun requestFollowUser(profileId: Long): DataResult<Boolean> {
        try {
            val request = http.requestFollowUser(userId = profileId)
            if (!request.isSuccessful) {
                return DataResult.Fail(code = request.code(), message = request.message())
            }
            return DataResult.Success(true)
        } catch (e: Exception) {
            return DataResult.Fail(code = APP_ERROR_CODE, message = e.message, throwable = e)
        }
    }

    override suspend fun requestUnFollowUser(profileId: Long): DataResult<Boolean> {
        try {
            val request = http.requestUnFollowUser(toMemberId = profileId)
            if (!request.isSuccessful) {
                return DataResult.Fail(code = request.code(), message = request.message())
            }
            return DataResult.Success(true)
        } catch (e: Exception) {
            return DataResult.Fail(code = APP_ERROR_CODE, message = e.message, throwable = e)
        }
    }

    override suspend fun requestBlockUser(profileId: Long): DataResult<Boolean> {
        try {
            val request = http.requestBlockMember(toMemberId = profileId)
            if (!request.isSuccessful) {
                return DataResult.Fail(code = request.code(), message = request.message())
            }
            return DataResult.Success(true)
        } catch (e: Exception) {
            return DataResult.Fail(code = APP_ERROR_CODE, message = e.message, throwable = e)
        }
    }

    override suspend fun requestUnBlockUser(profileId: Long): DataResult<Boolean> {
        try {
            val request = http.requestUnBlockMember(toMemberId = profileId)
            if (!request.isSuccessful) {
                return DataResult.Fail(code = request.code(), message = request.message())
            }
            return DataResult.Success(true)
        } catch (e: Exception) {
            return DataResult.Fail(code = APP_ERROR_CODE, message = e.message, throwable = e)
        }
    }

}