package com.phew.network.retrofit

import com.phew.network.NoAuth
import com.phew.network.dto.AppVersionStatusDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AppVersionHttp {
    /**
     * App version check API
     * GET /api/version/{type}
     */
    @NoAuth
    @GET("/api/version/{type}")
    suspend fun checkAppVersion(
        @Path("type") type: String,
        @Query("version") version: String
    ): Response<AppVersionStatusDTO>
}