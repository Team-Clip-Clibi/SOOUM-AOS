package com.phew.network.retrofit

import com.phew.network.BuildConfig
import com.phew.network.dto.response.card.ProfileCardDTO
import com.phew.network.dto.response.profile.MyProfileDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ProfileHttp {
    /**
     * 내 프로필 조회
     */
    @GET(BuildConfig.API_URL_MY_PROFILE)
    suspend fun requestMyProfile(): Response<MyProfileDTO>

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
}