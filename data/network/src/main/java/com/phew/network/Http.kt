package com.phew.network

import com.phew.network.dto.AppVersionDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface Http {
    /**
     * App version check url
     */
    @GET(BuildConfig.API_URL)
    suspend fun getVersion(
        @Path(BuildConfig.API_URL_TYPE) type: String,
        @Query(BuildConfig.API_URL_QUERY) data: String,
    ): Response<AppVersionDTO>

}