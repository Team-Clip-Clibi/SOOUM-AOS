package com.phew.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phew.core_common.DataResult
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.domain.dto.CardComment
import com.phew.domain.repository.network.CardDetailRepository
import java.io.IOException

internal class PagingCardComments(
    private val repository: CardDetailRepository,
    private val cardId: Long,
    private val latitude: Double?,
    private val longitude: Double?
) : PagingSource<Long, CardComment>() {

    override fun getRefreshKey(state: PagingState<Long, CardComment>): Long? {
        return null
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, CardComment> {
        val lastId = params.key
        return try {
            val result = if (lastId == null) {
                repository.getCardComments(cardId, latitude, longitude)
            } else {
                repository.getCardCommentsMore(cardId, lastId, latitude, longitude)
            }

            when (result) {
                is DataResult.Success -> {
                    val data = result.data
                    val nextKey = data.lastOrNull()?.cardId
                    LoadResult.Page(
                        data = data,
                        prevKey = null,
                        nextKey = nextKey
                    )
                }

                is DataResult.Fail -> {
                    val exception = if (result.code == HTTP_INVALID_TOKEN) {
                        SecurityException("Invalid token")
                    } else {
                        IOException(result.message ?: ERROR_NETWORK)
                    }
                    LoadResult.Error(exception)
                }
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
