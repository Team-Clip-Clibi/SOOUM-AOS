package com.phew.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.domain.dto.Notice
import com.phew.domain.dto.NoticeSource
import com.phew.domain.repository.network.NotifyRepository
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

            result.fold(
                onSuccess = { data ->
                    if (data.second.isEmpty() || data.first == HTTP_NO_MORE_CONTENT) {
                        return LoadResult.Page(
                            data = emptyList(),
                            prevKey = null,
                            nextKey = null
                        )
                    }
                    delay(2000L)
                    val isEndOfList = data.first == HTTP_NO_MORE_CONTENT ||
                            data.second.isEmpty() ||
                            data.second.size < params.loadSize
                    LoadResult.Page(
                        data = data.second,
                        prevKey = null,
                        nextKey = if (isEndOfList) null else data.second.last().id
                    )
                },
                onFailure = { LoadResult.Error(it) },
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
