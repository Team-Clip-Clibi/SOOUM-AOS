package com.phew.domain.repository.network

import com.phew.core_common.DataResult
import com.phew.domain.dto.CardImageDefault
import com.phew.domain.dto.Latest
import com.phew.domain.dto.Popular
import com.phew.domain.dto.TagInfo

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
}