package com.phew.network.retrofit

import com.phew.network.BuildConfig
import com.phew.network.dto.response.profile.MyProfileDTO
import retrofit2.Response
import retrofit2.http.GET

interface ProfileHttp {
    /**
     * 내 프로필 조회
     */
    @GET(BuildConfig.API_URL_MY_PROFILE)
    suspend fun requestMyProfile(): Response<MyProfileDTO>
}