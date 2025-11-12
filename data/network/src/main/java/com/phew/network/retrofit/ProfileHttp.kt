package com.phew.network.retrofit

import com.phew.network.BuildConfig
import com.phew.network.dto.response.card.ProfileCardDTO
import com.phew.network.dto.response.profile.FollowDataDTO
import com.phew.network.dto.response.profile.ProfileDTO
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProfileHttp {
    /**
     * 내 프로필 조회
     */
    @GET(BuildConfig.API_URL_MY_PROFILE)
    suspend fun requestMyProfile(): Response<ProfileDTO>

    /**
     * 내 프로필 피드 카드 조회
     */
    @GET(BuildConfig.API_URL_MY_PROFILE_CARD)
    suspend fun requestMyProfileFeedCard(
        @Path("userId") userId: Long,
    ): Response<ProfileCardDTO>

    /**
     * 내 프로필 카드 조회(Last ID)
     */
    @GET(BuildConfig.API_URL_MY_PROFILE_CARD)
    suspend fun requestMyProfileFeedCardNext(
        @Path("userId") userId: Long,
        @Path("lastId") lastId: Long,
    ): Response<ProfileCardDTO>

    /**
     * 내 프로필 답 카드 조회
     */
    @GET(BuildConfig.API_URL_MY_PROFILE_COMMENT_CARD)
    suspend fun requestMyProfileCommentCard(): Response<ProfileCardDTO>

    /**
     * 내 프로필 답 카드 조회
     */
    @GET(BuildConfig.API_URL_MY_PROFILE_COMMENT_CARD)
    suspend fun requestMyProfileCommentCardNext(
        @Path("lastId") lastId: Long,
    ): Response<ProfileCardDTO>

    /**
     * 내 프로필 팔로워 API
     */
    @GET(BuildConfig.API_URL_FOLLOWER)
    suspend fun requestFollower(
        @Path("profileOwnerId") profileOwnerId: Long,
    ): Response<List<FollowDataDTO>>

    @GET(BuildConfig.API_URL_FOLLOWER_NEXT)
    suspend fun requestFollowerNext(
        @Path("profileOwnerId") profileOwnerId: Long,
        @Path("lastId") lastId: Long,
    ): Response<List<FollowDataDTO>>

    /**
     * 내 프로필 팔로우 API
     */
    @GET(BuildConfig.API_URL_FOLLOWING)
    suspend fun requestFollowing(
        @Path("profileOwnerId") profileOwnerId: Long,
    ): Response<List<FollowDataDTO>>

    @GET(BuildConfig.API_URL_FOLLOWING_NEXT)
    suspend fun requestFollowingNext(
        @Path("profileOwnerId") profileOwnerId: Long,
        @Path("lastId") lastId: Long,
    ): Response<List<FollowDataDTO>>

    /**
     * 상대방 프로필 조회
     */
    @GET(BuildConfig.API_URL_OTHER_PROFILE)
    suspend fun requestOtherProfile(
        @Path("profileOwnerId") profileOwnerId: Long,
    ): Response<ProfileDTO>

    /**
     * 팔로우 API
     */
    @POST(BuildConfig.API_URL_FOLLOW_USER)
    suspend fun requestFollowUser(
        @Query("userId") userId: Long,
    ): Response<Unit>

    /**
     * 언 팔로우 API
     */
    @DELETE(BuildConfig.API_URL_UN_FOLLOW_USER)
    suspend fun requestUnFollowUser(
        @Path("toMemberId") toMemberId: Long,
    ): Response<Unit>

    /**
     *  멤버 차단
     */
    @POST(BuildConfig.API_URL_BLOCK_MEMBER)
    suspend fun requestBlockMember(
        @Path("toMemberId") toMemberId: Long,
    ): Response<Unit>

    /**
     *  멤버 차단 해제
     */
    @DELETE(BuildConfig.API_URL_UNBLOCK_MEMBER)
    suspend fun requestUnBlockMember(
        @Path("toMemberId") toMemberId: Long,
    ): Response<Unit>
}