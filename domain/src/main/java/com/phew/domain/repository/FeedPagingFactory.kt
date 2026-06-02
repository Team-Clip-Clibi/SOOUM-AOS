package com.phew.domain.repository

import androidx.paging.PagingData
import com.phew.domain.dto.FeedCardType
import kotlinx.coroutines.flow.Flow

interface FeedPagingFactory {
    fun create(query: FeedPagingQuery): Flow<PagingData<FeedCardType>>
}

sealed interface FeedPagingQuery {
    val latitude: Double?
    val longitude: Double?

    data class Latest(
        override val latitude: Double?,
        override val longitude: Double?,
    ) : FeedPagingQuery

    data class Popular(
        override val latitude: Double?,
        override val longitude: Double?,
    ) : FeedPagingQuery

    data class Distance(
        override val latitude: Double?,
        override val longitude: Double?,
        val distance: Double,
    ) : FeedPagingQuery
}
