package com.phew.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phew.domain.dto.FeedCardType
import com.phew.domain.repository.FeedPagingQuery
import com.phew.domain.repository.network.CardFeedRepository

internal class FeedPagingSource(
    private val repository: CardFeedRepository,
    private val query: FeedPagingQuery,
) : PagingSource<Long, FeedCardType>() {
    private val loadedCardIds = mutableSetOf<String>()

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
        return repository.requestFeedLatest(
            latitude = query.latitude,
            longitude = query.longitude,
            lastId = lastId
        ).getOrThrow()
            .map { it.toFeedCardType() }
            .filterNotLoaded()
            .toPage()
    }

    private suspend fun loadPopular(
        query: FeedPagingQuery.Popular,
        lastId: Long?,
    ): LoadResult<Long, FeedCardType> {
        if (lastId != null) {
            return emptyList<FeedCardType>().toPage()
        }

        return LoadResult.Page(
            data = repository.requestFeedPopular(
            latitude = query.latitude,
            longitude = query.longitude
        ).getOrThrow()
                .map { it.toFeedCardType() }
                .filterNotLoaded(),
            prevKey = null,
            nextKey = null
        )
    }

    private suspend fun loadDistance(
        query: FeedPagingQuery.Distance,
        lastId: Long?,
    ): LoadResult<Long, FeedCardType> {
        return repository.requestFeedDistance(
            latitude = query.latitude,
            longitude = query.longitude,
            distance = query.distance,
            lastId = lastId
        ).getOrThrow()
            .map { it.toFeedCardType() }
            .filterNotLoaded()
            .toPage()
    }

    private fun List<FeedCardType>.toPage(): LoadResult.Page<Long, FeedCardType> {
        val nextKey = asReversed()
            .firstNotNullOfOrNull { it.cardId.toLongOrNull() }

        return LoadResult.Page(
            data = this,
            prevKey = null,
            nextKey = nextKey
        )
    }

    private fun List<FeedCardType>.filterNotLoaded(): List<FeedCardType> {
        return filter { feedCard -> loadedCardIds.add(feedCard.cardId) }
    }
}

private val FeedCardType.cardId: String
    get() = when (this) {
        is FeedCardType.BoombType -> cardId
        is FeedCardType.AdminType -> cardId
        is FeedCardType.NormalType -> cardId
    }
