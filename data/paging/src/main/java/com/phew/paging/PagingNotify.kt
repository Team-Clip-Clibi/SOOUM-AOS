package com.phew.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phew.core_common.DataResult
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.domain.dto.Notice
import com.phew.domain.repository.network.NotifyRepository
import javax.inject.Inject

class PagingNotify @Inject constructor(
    private val notifyRepository: NotifyRepository
) : PagingSource<Int, Notice>() {
    override fun getRefreshKey(state: PagingState<Int, Notice>): Int {
        return -1
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Notice> {
        return try {
            val key = params.key ?: -1
            val result = if (key == -1) {
                notifyRepository.requestNotice()
            } else {
                notifyRepository.requestNoticePatch(
                    lastId = key
                )
            }
            when (result) {
                is DataResult.Success -> {
                    val responseCode = result.data.first
                    if (responseCode == HTTP_NO_MORE_CONTENT || result.data.second.isEmpty()) {
                        return LoadResult.Page(
                            data = emptyList(),
                            prevKey = null,
                            nextKey = null
                        )
                    }
                    LoadResult.Page(
                        data = result.data.second,
                        prevKey = null,
                        nextKey = result.data.second.last().id
                    )
                }

                is DataResult.Fail -> {
                    if (result.code != HTTP_INVALID_TOKEN) {
                        return LoadResult.Error(Throwable(ERROR_NETWORK))
                    }
                    return LoadResult.Page(
                        data = emptyList(),
                        prevKey = null,
                        nextKey = null
                    )
                }
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}