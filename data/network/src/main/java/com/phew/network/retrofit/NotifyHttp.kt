package com.phew.network.retrofit

import com.phew.network.BuildConfig
import com.phew.network.dto.NoticeDto
import com.phew.network.dto.NotificationDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NotifyHttp {

    /**
     * Notice url
     */
    @GET(BuildConfig.API_URL_NOTICE)
    suspend fun requestNotice(): Response<NoticeDto>

    @GET(BuildConfig.API_URL_NOTICE)
    suspend fun requestNoticePatch(
        @Query("lastId") lastId: Int,
    ): Response<NoticeDto>

    /**
     * Notification url
     */
    @GET(BuildConfig.API_URL_NOTIFICATION_UN_READ)
    suspend fun requestNotificationUnRead(): Response<List<NotificationDTO>>

    @GET(BuildConfig.API_URL_NOTIFICATION_UN_READ)
    suspend fun requestNotificationUnReadPatch(
        @Query("lastId") lastId: Long,
    ): Response<List<NotificationDTO>>

    @GET(BuildConfig.API_URL_NOTIFICATION_READ)
    suspend fun requestNotificationRead(): Response<List<NotificationDTO>>

    @GET(BuildConfig.API_URL_NOTIFICATION_READ)
    suspend fun requestNotificationReadPatch(
        @Query("lastId") lastId: Long
    ): Response<List<NotificationDTO>>
}