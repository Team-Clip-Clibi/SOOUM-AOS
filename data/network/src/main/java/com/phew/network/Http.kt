package com.phew.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface Http {
    /**
     * App version check url
     */
    @GET(BuildConfig.API_URL)
    suspend fun getVersion(
        @Path("type") type: String,
    ): Response<String>

}