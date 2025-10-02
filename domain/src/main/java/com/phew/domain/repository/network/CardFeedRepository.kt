package com.phew.domain.repository.network

import com.phew.core_common.DataResult
import com.phew.domain.dto.Latest
import com.phew.domain.dto.Popular

interface CardFeedRepository {
    suspend fun requestFeedPopular(
        accessToken: String, 
        latitude: Double? = null, 
        longitude: Double? = null
    ): DataResult<List<Popular>>
    
    suspend fun requestFeedLatest(
        accessToken: String, 
        latitude: Double? = null, 
        longitude: Double? = null, 
        lastId: Int? = null
    ): DataResult<List<Latest>>
}