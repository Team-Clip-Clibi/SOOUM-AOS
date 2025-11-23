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
        return null // 항상 첫 페이지부터 시작하여 최신 데이터를 상단에 표시
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