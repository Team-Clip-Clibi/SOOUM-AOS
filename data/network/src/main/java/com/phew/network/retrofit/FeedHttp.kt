package com.phew.network.retrofit

import com.phew.network.dto.response.LatestDto
import com.phew.network.dto.response.PopularDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface FeedHttp {
    /**
     * Popular Feed url
     */
    @GET(BuildConfig.API_URL_CARD_FEED_POPULAR)
    suspend fun requestPopularFeed(
        @Header("Authorization") bearerToken: String,
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double
    ): Response<List<PopularDto>>

    /**
     * Latest Feed url
     */
    @GET(BuildConfig.API_URL_CARD_FEED_LATEST)
    suspend fun requestLatestFeed(
        @Header("Authorization") bearerToken: String,
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double
    ): Response<List<LatestDto>>

    /**
     * Latest Feed Last url
     */
    @GET(BuildConfig.API_URL_CARD_FEED_LATEST_LAST)
    suspend fun requestLatestFeedLast(
        @Header("Authorization") bearerToken: String,
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double,
        @Path("lastId") lastId: Long
    ): Response<List<LatestDto>>

}