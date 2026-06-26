package com.phew.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.exception.asSooumException
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.HTTP_NO_MORE_CONTENT
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

            result.fold(
                onSuccess = { data ->
                    val nextKey = data.lastOrNull()?.cardId
                    LoadResult.Page(
                        data = data,
                        prevKey = null,
                        nextKey = nextKey
                    )
                },
                onFailure = { throwable ->
                    val exception = throwable.asSooumException()
                    if (exception.code == HTTP_NO_MORE_CONTENT) {
                        return LoadResult.Page(
                            data = emptyList(),
                            prevKey = null,
                            nextKey = null
                        )
                    }
                    val error = if (exception.code == HTTP_INVALID_TOKEN) {
                        SecurityException("Invalid token")
                    } else {
                        IOException(exception.message.ifBlank { ERROR_NETWORK })
                    }
                    LoadResult.Error(error)
                },
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
