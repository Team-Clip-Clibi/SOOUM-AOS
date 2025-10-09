package com.phew.network.retrofit

import com.phew.network.BuildConfig
import com.phew.network.NoAuth
import com.phew.network.dto.TokenDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Token Refresh API
 */
interface TokenRefreshHttp {
    @NoAuth
    @POST(BuildConfig.API_URL_REFRESH_TOKEN)
    suspend fun requestRefreshToken(
        @Body body: TokenDTO,
    ): Response<TokenDTO>
}