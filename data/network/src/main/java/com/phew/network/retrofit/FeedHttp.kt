package com.phew.network.retrofit

import com.phew.network.BuildConfig
import com.phew.network.dto.TagRequestDTO
import com.phew.network.dto.request.feed.DefaultImageDTO
import com.phew.network.dto.request.feed.TagInfoListDTO
import com.phew.network.dto.response.LatestDto
import com.phew.network.dto.response.PopularDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FeedHttp {
    /**
     * Popular Feed url
     */
    @GET(BuildConfig.API_URL_CARD_FEED_POPULAR)
    suspend fun requestPopularFeed(
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null
    ): Response<List<PopularDto>>

    /**
     * Latest Feed url
     */
    @GET(BuildConfig.API_URL_CARD_FEED_LATEST)
    suspend fun requestLatestFeed(
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
        @Query("lastId") lastId: Int? = null
    ): Response<List<LatestDto>>

    /**
     * related Tag url
     */
    @GET(BuildConfig.API_URL_TAG_RELATED)
    suspend fun requestRelatedTag(
        @Path("resultCnt") resultCnt: Int,
        @Body request: TagRequestDTO
    ): Response<TagInfoListDTO>

    @GET(BuildConfig.API_URL_CARD_IMAGE_DEFAULT)
    suspend fun requestCardImageDefault(): Response<DefaultImageDTO>
}