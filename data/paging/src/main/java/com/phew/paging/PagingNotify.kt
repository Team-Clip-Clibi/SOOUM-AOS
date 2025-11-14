package com.phew.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phew.core_common.DataResult
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.domain.dto.Notice
import com.phew.domain.dto.NoticeSource
import com.phew.domain.repository.network.NotifyRepository
import java.io.IOException
import javax.inject.Inject

class PagingNotify @Inject constructor(
    private val notifyRepository: NotifyRepository,
    private val source: NoticeSource = NoticeSource.SETTINGS,
) : PagingSource<Int, Notice>() {

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
                    val originalNotifyList = result.data.second.sortedBy { data -> data.id }
                    val lastItemId = originalNotifyList.lastOrNull()?.id

                    val nextKey = if (originalNotifyList.isEmpty() || lastItemId == key) {
                        null
                    } else {
                        originalNotifyList.last().id
                    }

                    LoadResult.Page(
                        data = originalNotifyList,
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