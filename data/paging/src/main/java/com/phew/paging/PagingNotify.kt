package com.phew.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phew.core_common.DataResult
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.domain.dto.Notice
import com.phew.domain.dto.NoticeSource
import com.phew.domain.repository.network.NotifyRepository
import java.io.IOException
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay

class PagingNotify @AssistedInject constructor(
    private val notifyRepository: NotifyRepository,
    @Assisted private val source: NoticeSource,
) : PagingSource<Int, Notice>() {

    @AssistedFactory
    interface Factory {
        fun create(source: NoticeSource): PagingNotify
    }

    override fun getRefreshKey(state: PagingState<Int, Notice>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey
                ?: state.closestPageToPosition(anchorPosition)?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Notice> {
        val key = params.key ?: -1

        return try {
            val result = if (key == -1) {
                notifyRepository.requestNotice(pageSize = 30, source = source)
            } else {
                notifyRepository.requestNoticePatch(lastId = key, pageSize = 30, source = source)
            }

            when (result) {
                is DataResult.Success -> {
                    if (result.data.second.isEmpty() || result.data.first == HTTP_NO_MORE_CONTENT) {
                        return LoadResult.Page(
                            data = emptyList(),
                            prevKey = null,
                            nextKey = null
                        )
                    }
                    delay(2000L)
                    val isEndOfList = result.data.first == HTTP_NO_MORE_CONTENT ||
                            result.data.second.isEmpty() ||
                            result.data.second.size < params.loadSize
                    LoadResult.Page(
                        data = result.data.second,
                        prevKey = null,
                        nextKey = if (isEndOfList) null else result.data.second.last().id
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
