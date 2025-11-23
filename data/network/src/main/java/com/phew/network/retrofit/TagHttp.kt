package com.phew.network.retrofit

import com.phew.network.BuildConfig
import com.phew.network.dto.TagRequestDTO
import com.phew.network.dto.request.feed.TagInfoListDTO
import com.phew.network.dto.response.FavoriteTagsResponseDTO
import com.phew.network.dto.response.TagCardsResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TagHttp {
    @POST("${BuildConfig.API_URL_TAGS}/{tagId}/favorite")
    suspend fun addFavoriteTag(@Path("tagId") tagId: Long): Response<Unit>

    @DELETE("${BuildConfig.API_URL_TAGS}/{tagId}/favorite")
    suspend fun removeFavoriteTag(@Path("tagId") tagId: Long): Response<Unit>

    @POST(BuildConfig.API_URL_TAG_RELATED)
    suspend fun getRelatedTags(
        @Path("resultCnt") resultCnt: Long,
        @Body request: TagRequestDTO
    ): Response<TagInfoListDTO>

    @GET("${BuildConfig.API_URL_TAGS}/{tagId}/cards/{lastId}")
    suspend fun getTagCards(
        @Path("tagId") tagId: Long,
        @Path("lastId") lastId: Long
    ): Response<TagCardsResponseDTO>

    @GET("${BuildConfig.API_URL_TAGS}/{tagId}/cards")
    suspend fun getTagCardsWithFavorite(
        @Path("tagId") tagId: Long
    ): Response<TagCardsResponseDTO>

    @GET(BuildConfig.API_URL_TAG_RANK)
    suspend fun getTagRank(): Response<TagInfoListDTO>

    @GET(BuildConfig.API_URL_TAG_FAVORITE)
    suspend fun getFavoriteTags(): Response<FavoriteTagsResponseDTO>
}