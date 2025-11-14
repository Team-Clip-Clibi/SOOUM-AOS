package com.phew.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phew.core_common.DataResult
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.domain.dto.Latest
import com.phew.domain.repository.network.CardFeedRepository
import java.io.IOException
import javax.inject.Inject

class PagingLatestFeed @Inject constructor(
    private val cardFeedRepository: CardFeedRepository,
    private val latitude: Double?,
    private val longitude: Double?,
) : PagingSource<Int, Latest>() {

    override fun getRefreshKey(state: PagingState<Int, Latest>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey
                ?: state.closestPageToPosition(anchorPosition)?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Latest> {
        val lastId = params.key

        return try {
            val result = cardFeedRepository.requestFeedLatest(
                latitude = latitude,
                longitude = longitude,
                lastId = lastId
            )

            when (result) {
                is DataResult.Success -> {
                    val latestList = result.data.sortedBy { data -> data.cardId }

                    if (latestList.isEmpty()) {
                        return LoadResult.Page(
                            data = latestList,
                            prevKey = null,
                            nextKey = null
                        )
                    }

                    LoadResult.Page(
                        data = latestList,
                        prevKey = null,
                        nextKey = latestList.lastOrNull()?.cardId?.toIntOrNull()
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