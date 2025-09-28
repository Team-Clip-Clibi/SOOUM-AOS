package com.phew.network

import com.phew.network.dto.AppVersionDTO
import com.phew.network.dto.CheckSignUpDTO
import com.phew.network.dto.FCMToken
import com.phew.network.dto.InfoDTO
import com.phew.network.dto.NickNameDTO
import com.phew.network.dto.SecurityKeyDTO
import com.phew.network.dto.SignUpRequest
import com.phew.network.dto.TokenDTO
import com.phew.network.dto.NickNameAvailableDTO
import com.phew.network.dto.UploadImageUrlDTO
import com.phew.network.dto.NoticeDto
import com.phew.network.dto.NotificationDTO
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface Http {
    /**
     * App version check url
     */
    @GET(BuildConfig.API_URL)
    suspend fun getVersion(
        @Path(BuildConfig.API_URL_TYPE) type: String,
        @Query(BuildConfig.API_URL_QUERY) data: String,
    ): Response<AppVersionDTO>

    /**
     * Security key url
     */
    @GET(BuildConfig.API_SECURITY_KEY)
    suspend fun getSecurityKey(): Response<SecurityKeyDTO>

    /**
     * SignUp URL
     */
    @POST(BuildConfig.API_URL_CHECK_SIGN_UP)
    suspend fun requestCheckSignUp(
        @Body body: InfoDTO,
    ): Response<CheckSignUpDTO>

    /**
     * Login url
     */
    @GET(BuildConfig.API_URL_LOGIN)
    suspend fun requestLogin(
        @Body body: InfoDTO,
    ): Response<TokenDTO>

    /**
     * Update FCM url
     */
    @PATCH(BuildConfig.API_URL_FCM_UPDATE)
    suspend fun requestUpdateFcm(
        @Header("Authorization") bearerToken: String,
        @Body body: FCMToken,
    ): Response<Unit>

    /**
     * Sign up url
     */
    @POST(BuildConfig.API_URL_SIGN_UP)
    suspend fun requestSignUp(
        @Body body: SignUpRequest,
    ): Response<TokenDTO>

    /**
     * NickName Generator url
     */
    @GET(BuildConfig.API_URL_NICKNAME_GENERATOR)
    suspend fun requestNickNameGenerator(): Response<NickNameDTO>

    /**
     * Check NickName value
     */
    @POST(BuildConfig.API_URL_CHECK_NICKNAME_AVAILABLE)
    suspend fun requestCheckNickName(
        @Body body: NickNameDTO,
    ): Response<NickNameAvailableDTO>

    /**
     * get Upload Image Url
     */
    @POST(BuildConfig.API_URL_UPLOAD_IMAGE)
    suspend fun requestUploadImageUrl(): Response<UploadImageUrlDTO>

    /**
     * upload image url
     */
    @PUT
    suspend fun requestUploadImage(
        @Url url: String,
        @Body body: RequestBody,
    ): Response<Unit>

    /**
     * Refresh Token url
     */
    @POST(BuildConfig.API_URL_REFRESH_TOKEN)
    suspend fun requestRefreshToken(
        @Body body: TokenDTO,
    ): Response<TokenDTO>

    /**
     * Notice url
     */
    @GET(BuildConfig.API_URL_NOTICE)
    suspend fun requestNotice(
        @Header("Authorization") bearerToken: String,
    ): Response<NoticeDto>

    @GET(BuildConfig.API_URL_NOTICE)
    suspend fun requestNoticePatch(
        @Header("Authorization") bearerToken: String,
        @Path("lastId") lastId: Int,
    ): Response<NoticeDto>

    /**
     * Notification url
     */
    @GET(BuildConfig.API_URL_NOTIFICATION_UN_READ)
    suspend fun requestNotificationUnRead(
        @Header("Authorization") bearerToken: String,
    ): Response<List<NotificationDTO>>

    @GET(BuildConfig.API_URL_NOTIFICATION_UN_READ)
    suspend fun requestNotificationUnReadPatch(
        @Header("Authorization") bearerToken: String,
        @Path("lastId") lastId: Long,
    ): Response<List<NotificationDTO>>

    @GET(BuildConfig.API_URL_NOTIFICATION_READ)
    suspend fun requestNotificationRead(
        @Header("Authorization") bearerToken: String,
    ): Response<List<NotificationDTO>>

    @GET(BuildConfig.API_URL_NOTIFICATION_READ)
    suspend fun requestNotificationReadPatch(
        @Header("Authorization") bearerToken: String,
        @Path("lastId") lastId: Long,
    ): Response<List<NotificationDTO>>

}