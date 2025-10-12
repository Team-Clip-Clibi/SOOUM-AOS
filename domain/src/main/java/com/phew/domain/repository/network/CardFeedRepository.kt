package com.phew.domain.repository.network

import com.phew.core_common.DataResult
import com.phew.domain.dto.CardImageDefault
import com.phew.domain.dto.CheckedBaned
import com.phew.domain.dto.Latest
import com.phew.domain.dto.Popular
import com.phew.domain.dto.TagInfo
import okhttp3.RequestBody

interface CardFeedRepository {
    suspend fun requestFeedPopular(
        latitude: Double? = null,
        longitude: Double? = null
    ): DataResult<List<Popular>>

    suspend fun requestFeedLatest(
        latitude: Double? = null,
        longitude: Double? = null,
        lastId: Int? = null
    ): DataResult<List<Latest>>

    suspend fun requestRelatedTag(resultCnt: Int = 8, tag: String): DataResult<List<TagInfo>>
    suspend fun requestCardImageDefault(): DataResult<List<CardImageDefault>>
    suspend fun requestUploadCardImage(): DataResult<CardImageDefault>
    suspend fun requestCheckUploadCard(): DataResult<CheckedBaned>
    suspend fun requestUploadCard(
        isDistanceShared: Boolean,
        latitude: Double?,
        longitude: Double?,
        content: String,
        font: String,
        imageType: String,
        imageName: String,
        isStory: Boolean,
        tag: List<String>
    ): Int

    suspend fun requestUploadCardAnswer(
        cardId : Int,
        isDistanceShared: Boolean,
        latitude: Double?,
        longitude: Double?,
        content: String,
        font: String,
        imageType: String,
        imageName: String,
        tag: List<String>
    ): Int

    suspend fun requestUploadImage(data: RequestBody, url: String): DataResult<Unit>
}