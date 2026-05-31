package com.phew.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phew.core_common.DataResult
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.domain.dto.DistanceCard
import com.phew.domain.dto.FeedCardType
import com.phew.domain.dto.Latest
import com.phew.domain.repository.FeedPagingQuery
import com.phew.domain.repository.network.CardFeedRepository
import java.io.IOException

internal class FeedPagingSource(
    private val repository: CardFeedRepository,
    private val query: FeedPagingQuery,
) : PagingSource<Long, FeedCardType>() {

    override fun getRefreshKey(state: PagingState<Long, FeedCardType>): Long? = null

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, FeedCardType> {
        val lastId = params.key

        return try {
            when (query) {
                is FeedPagingQuery.Latest -> loadLatest(query, lastId)
                is FeedPagingQuery.Popular -> loadPopular(query, lastId)
                is FeedPagingQuery.Distance -> loadDistance(query, lastId)
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private suspend fun loadLatest(
        query: FeedPagingQuery.Latest,
        lastId: Long?,
    ): LoadResult<Long, FeedCardType> {
        return when (val result = repository.requestFeedLatest(
            latitude = query.latitude,
            longitude = query.longitude,
            lastId = lastId
        )) {
            is DataResult.Success -> result.data
                .filterNotLastId(lastId)
                .map { it.toFeedCardType() }
                .toPage(lastId = lastId)

            is DataResult.Fail -> result.toError()
        }
    }

    private suspend fun loadPopular(
        query: FeedPagingQuery.Popular,
        lastId: Long?,
    ): LoadResult<Long, FeedCardType> {
        if (lastId != null) {
            return emptyList<FeedCardType>().toPage(lastId = lastId)
        }

        return when (val result = repository.requestFeedPopular(
            latitude = query.latitude,
            longitude = query.longitude
        )) {
            is DataResult.Success -> LoadResult.Page(
                data = result.data.map { it.toFeedCardType() },
                prevKey = null,
                nextKey = null
            )

            is DataResult.Fail -> result.toError()
        }
    }

    private suspend fun loadDistance(
        query: FeedPagingQuery.Distance,
        lastId: Long?,
    ): LoadResult<Long, FeedCardType> {
        return when (val result = repository.requestFeedDistance(
            latitude = query.latitude,
            longitude = query.longitude,
            distance = query.distance,
            lastId = lastId
        )) {
            is DataResult.Success -> result.data
                .filterNotLastId(lastId)
                .map { it.toFeedCardType() }
                .toPage(lastId = lastId)

            is DataResult.Fail -> result.toError()
        }
    }

    private fun List<FeedCardType>.toPage(
        lastId: Long?,
    ): LoadResult.Page<Long, FeedCardType> {
        val nextKey = asReversed()
            .firstNotNullOfOrNull { it.cardId.toLongOrNull() }
            ?.takeIf { it != lastId }

        return LoadResult.Page(
            data = this,
            prevKey = null,
            nextKey = nextKey
        )
    }

    private fun <T : Any> DataResult.Fail.toError(): LoadResult.Error<Long, T> {
        val exception = if (code == HTTP_INVALID_TOKEN) {
            SecurityException("Invalid Token")
        } else {
            IOException(message ?: ERROR_NETWORK)
        }
        return LoadResult.Error<Long, T>(exception)
    }
}

private val FeedCardType.cardId: String
    get() = when (this) {
        is FeedCardType.BoombType -> cardId
        is FeedCardType.AdminType -> cardId
        is FeedCardType.NormalType -> cardId
    }

private fun <T> List<T>.filterNotLastId(lastId: Long?): List<T> {
    if (lastId == null) return this
    return filterNot { item ->
        val cardId = when (item) {
            is Latest -> item.cardId
            is DistanceCard -> item.cardId
            else -> null
        }
        cardId?.toLongOrNull() == lastId
    }
}
