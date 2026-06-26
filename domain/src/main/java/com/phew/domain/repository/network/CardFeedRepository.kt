package com.phew.domain.repository.network

import com.phew.domain.dto.CardArticle
import com.phew.domain.dto.CardDefaultImagesResponse
import com.phew.domain.dto.CardIdResponse
import com.phew.domain.dto.CardImageDefault
import com.phew.domain.dto.CheckedBaned
import com.phew.domain.dto.DistanceCard
import com.phew.domain.dto.Latest
import com.phew.domain.dto.Popular
import com.phew.domain.dto.TagInfo
import okhttp3.RequestBody

interface CardFeedRepository {
    suspend fun requestFeedPopular(
        latitude: Double? = null,
        longitude: Double? = null,
    ): Result<List<Popular>>

    suspend fun requestFeedLatest(
        latitude: Double? = null,
        longitude: Double? = null,
        lastId: Long? = null,
    ): Result<List<Latest>>

    suspend fun requestFeedDistance(
        latitude: Double? = null,
        longitude: Double? = null,
        distance: Double? = null,
        lastId: Long? = null,
    ): Result<List<DistanceCard>>

    suspend fun requestRelatedTag(resultCnt: Int = 8, tag: String): Result<List<TagInfo>>
    suspend fun requestCardImageDefault(): Result<CardDefaultImagesResponse>
    suspend fun requestUploadCardImage(): Result<CardImageDefault>
    suspend fun requestCheckUploadCard(): Result<CheckedBaned>
    suspend fun requestUploadCard(
        isDistanceShared: Boolean,
        latitude: Double?,
        longitude: Double?,
        content: String,
        font: String,
        imageType: String,
        imageName: String,
        isStory: Boolean,
        tag: List<String>,
        hasPoll: Boolean,
        pollContents: List<String>,
    ): Result<CardIdResponse>

    suspend fun requestUploadCardAnswer(
        cardId: Long,
        isDistanceShared: Boolean,
        latitude: Double?,
        longitude: Double?,
        content: String,
        font: String,
        imageType: String,
        imageName: String,
        tag: List<String>,
    ): Result<CardIdResponse>

    suspend fun requestUploadImage(data: RequestBody, url: String): Result<Unit>
    suspend fun requestCheckImage(imageName: String): Result<Boolean>
    suspend fun requestCheckCardDelete(cardId: Long): Result<Boolean>
    suspend fun requestCardArticle(): Result<CardArticle>
}
