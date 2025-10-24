package com.phew.network.retrofit

import com.phew.network.BuildConfig
import com.phew.network.dto.TagRequestDTO
import com.phew.network.dto.request.feed.CheckBanedDTO
import com.phew.network.dto.request.feed.DefaultImageDTO
import com.phew.network.dto.request.feed.ImageInfoDTO
import com.phew.network.dto.request.feed.RequestUploadCardAnswerDTO
import com.phew.network.dto.request.feed.RequestUploadCardDTO
import com.phew.network.dto.request.feed.TagInfoListDTO
import com.phew.network.dto.response.BackgroundImageDTO
import com.phew.network.dto.response.DistanceDTO
import com.phew.network.dto.response.LatestDto
import com.phew.network.dto.response.PopularDto
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface FeedHttp {
    /**
     * Popular Feed url
     */
    @GET(BuildConfig.API_URL_CARD_FEED_POPULAR)
    suspend fun requestPopularFeed(
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
    ): Response<List<PopularDto>>

    /**
     * Latest Feed url
     */
    @GET(BuildConfig.API_URL_CARD_FEED_LATEST)
    suspend fun requestLatestFeed(
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
        @Query("lastId") lastId: Int? = null,
    ): Response<List<LatestDto>>

    /**
     * Distance Feed url
     */
    @GET(BuildConfig.API_URL_CARD_FEED_DISTANCE)
    suspend fun requestDistanceFeed(
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
        @Query("distance") distance: Double? = null,
        @Query("lastId") lastId: Int? = null,
    ): Response<List<DistanceDTO>>

    /**
     * related Tag url
     */
    @POST(BuildConfig.API_URL_TAG_RELATED)
    suspend fun requestRelatedTag(
        @Path("resultCnt") resultCnt: Int,
        @Body request: TagRequestDTO,
    ): Response<TagInfoListDTO>

    /**
     * card background image Default url
     */
    @GET(BuildConfig.API_URL_CARD_IMAGE_DEFAULT)
    suspend fun requestCardImageDefault(): Response<DefaultImageDTO>

    /**
     * card background image Upload url
     */
    @GET(BuildConfig.API_URL_UPLOAD_CARD_IMAGE)
    suspend fun requestUploadCardUrl(): Response<ImageInfoDTO>

    /**
     * Card Upload url
     */
    @POST(BuildConfig.API_URL_UPLOAD_CARD)
    suspend fun requestUploadCard(
        @Body request: RequestUploadCardDTO,
    ): Response<Unit>

    /**
     * Card answer Upload url
     */
    @POST(BuildConfig.API_URL_UPLOAD_CARD_ANSWER)
    suspend fun requestUploadAnswerCard(
        @Path("cardId") cardId: Long,
        @Body request: RequestUploadCardAnswerDTO,
    ): Response<Unit>

    /**
     * checked user baned upload card
     */
    @GET(BuildConfig.API_URL_CHECKED_BANED)
    suspend fun requestCheckUploadCard(): Response<CheckBanedDTO>

    /**
     * Upload card background image
     */
    @PUT
    suspend fun requestUploadImage(
        @Url url: String,
        @Body body: RequestBody,
    ): Response<Unit>

    /**
     * Check upload background image
     */
    @GET(BuildConfig.API_URL_UPLOAD_BACKGROUND_IMAGE_CHECK)
    suspend fun requestCheckBackgroundImage(
        @Path("imgName") imgName: String,
    ): Response<BackgroundImageDTO>
}