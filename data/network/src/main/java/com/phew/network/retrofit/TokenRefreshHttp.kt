package com.phew.network.retrofit

import com.phew.network.BuildConfig
import com.phew.network.NoAuth
import com.phew.network.dto.InfoDTO
import com.phew.network.dto.SecurityKeyDTO
import com.phew.network.dto.TokenDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
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

    /**
     * RSA 보안 키 제공
     */
    @NoAuth
    @GET(BuildConfig.API_SECURITY_KEY)
    suspend fun getSecurityKey(): Response<SecurityKeyDTO>

    /**
     * 로그인 api
     */
    @NoAuth
    @POST(BuildConfig.API_URL_LOGIN)
    suspend fun requestLogin(
        @Body body: InfoDTO,
    ): Response<TokenDTO>

}