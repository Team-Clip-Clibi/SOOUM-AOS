package com.phew.network.retrofit

import com.phew.network.BuildConfig
import com.phew.network.NoAuth
import com.phew.network.dto.CheckSignUpDTO
import com.phew.network.dto.InfoDTO
import com.phew.network.dto.NickNameAvailableDTO
import com.phew.network.dto.NickNameDTO
import com.phew.network.dto.SecurityKeyDTO
import com.phew.network.dto.SignUpRequest
import com.phew.network.dto.TokenDTO
import com.phew.network.dto.UploadImageUrlDTO
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Url

interface SignUpHttp {
    /**
     * SignUp URL
     */
    @NoAuth
    @POST(BuildConfig.API_URL_CHECK_SIGN_UP)
    suspend fun requestCheckSignUp(
        @Body body: InfoDTO,
    ): Response<CheckSignUpDTO>

    /**
     * Login url
     */
    @NoAuth
    @POST(BuildConfig.API_URL_LOGIN)
    suspend fun requestLogin(
        @Body body: InfoDTO,
    ): Response<TokenDTO>

    /**
     * Sign up url
     */
    @NoAuth
    @POST(BuildConfig.API_URL_SIGN_UP)
    suspend fun requestSignUp(
        @Body body: SignUpRequest,
    ): Response<TokenDTO>

    /**
     * NickName Generator url
     */
    @NoAuth
    @GET(BuildConfig.API_URL_NICKNAME_GENERATOR)
    suspend fun requestNickNameGenerator(): Response<NickNameDTO>

    /**
     * Check NickName value
     */
    @NoAuth
    @POST(BuildConfig.API_URL_CHECK_NICKNAME_AVAILABLE)
    suspend fun requestCheckNickName(
        @Body body: NickNameDTO,
    ): Response<NickNameAvailableDTO>

    /**
     * get Upload Image Url
     */
    @NoAuth
    @POST(BuildConfig.API_URL_UPLOAD_IMAGE)
    suspend fun requestUploadImageUrl(): Response<UploadImageUrlDTO>

    /**
     * upload image url
     */
    @NoAuth
    @PUT
    suspend fun requestUploadImage(
        @Url url: String,
        @Body body: RequestBody,
    ): Response<Unit>

    /**
     * Security key url
     */
    @NoAuth
    @GET(BuildConfig.API_SECURITY_KEY)
    suspend fun getSecurityKey(): Response<SecurityKeyDTO>

}