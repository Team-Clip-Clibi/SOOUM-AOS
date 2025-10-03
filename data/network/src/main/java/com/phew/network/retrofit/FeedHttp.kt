package com.phew.network.retrofit

import com.phew.network.BuildConfig
import com.phew.network.dto.response.LatestDto
import com.phew.network.dto.response.PopularDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface FeedHttp {
    /**
     * Popular Feed url
     */
    @GET(BuildConfig.API_URL_CARD_FEED_POPULAR)
    suspend fun requestPopularFeed(
        @Header("Authorization") bearerToken: String,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null
    ): Response<List<PopularDto>>

    /**
     * Latest Feed url
     */
    @GET(BuildConfig.API_URL_CARD_FEED_LATEST)
    suspend fun requestLatestFeed(
        @Header("Authorization") bearerToken: String,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
        @Query("lastId") lastId: Int? = null
    ): Response<List<LatestDto>>
}