package com.phew.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phew.core_common.DataResult
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.log.SooumLog
import com.phew.domain.dto.Latest
import com.phew.domain.repository.network.CardFeedRepository
import java.io.IOException
import javax.inject.Inject

class PagingLatestFeed @Inject constructor(
    private val cardFeedRepository: CardFeedRepository,
    private val latitude: Double?,
    private val longitude: Double?,
) : PagingSource<Long, Latest>() {

    override fun getRefreshKey(state: PagingState<Long, Latest>): Long? {
        val anchorPosition = state.anchorPosition ?: return null
        return state.closestItemToPosition(anchorPosition)?.cardId?.toLongOrNull()
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Latest> {
        val lastId = params.key
        SooumLog.d(TAG, "load(lastId=$lastId, loadSize=${params.loadSize})")

        return try {
            when (val result = cardFeedRepository.requestFeedLatest(
                latitude = latitude,
                longitude = longitude,
                lastId = lastId
            )) {
                is DataResult.Success -> {
                    val feeds = result.data
                    val nextKey = feeds.asReversed()
                        .firstOrNull { it.cardId.toLongOrNull() != null }
                        ?.cardId
                        ?.toLongOrNull()
                        ?.takeIf { it != lastId }
                    LoadResult.Page(
                        data = feeds,
                        prevKey = null,
                        nextKey = nextKey
                    )
                }

                is DataResult.Fail -> {
                    val exception = if (result.code == HTTP_INVALID_TOKEN) {
                        SecurityException("Invalid Token")
                    } else {
                        IOException(ERROR_NETWORK)
                    }
                    LoadResult.Error(exception)
                }
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}

private const val TAG = "PagingLatestFeed"