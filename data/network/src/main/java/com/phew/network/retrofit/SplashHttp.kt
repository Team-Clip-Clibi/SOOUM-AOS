package com.phew.network.retrofit

import com.phew.network.BuildConfig
import com.phew.network.NoAuth
import com.phew.network.dto.AppVersionDTO
import com.phew.network.dto.FCMToken
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface SplashHttp {
    /**
     * App version check url
     */
    @NoAuth
    @GET(BuildConfig.API_URL)
    suspend fun getVersion(
        @Path("type") type: String,
        @Query("version") data: String,
    ): Response<AppVersionDTO>

    /**
     * Update FCM url
     */
    @PATCH(BuildConfig.API_URL_FCM_UPDATE)
    suspend fun requestUpdateFcm(
        @Body body: FCMToken,
    ): Response<Unit>
}