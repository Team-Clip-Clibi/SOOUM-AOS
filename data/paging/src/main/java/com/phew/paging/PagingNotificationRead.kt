package com.phew.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phew.core_common.DataResult
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.domain.dto.Notification
import com.phew.domain.repository.network.NotifyRepository
import javax.inject.Inject

class PagingNotificationRead @Inject constructor(
    private val notifyRepository: NotifyRepository
) : PagingSource<Long, Notification>() {

    override fun getRefreshKey(state: PagingState<Long, Notification>): Long? {
        return -1
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Notification> {
        try {
            val key = params.key ?: -1
            val result = if (key == -1L) {
                notifyRepository.requestNotificationRead()
            } else {
                notifyRepository.requestNotificationReadPatch(
                    lastId = key
                )
            }
            when (result) {
                is DataResult.Fail -> {
                    if (result.code != HTTP_INVALID_TOKEN) {
                        return LoadResult.Error(Throwable(ERROR_NETWORK))
                    }
                    return LoadResult.Page(data = emptyList(), prevKey = null, nextKey = null)
                }

                is DataResult.Success -> {
                    val data = result.data
                    if (data.second.isEmpty() && data.first == HTTP_NO_MORE_CONTENT) {
                        return LoadResult.Page(data = emptyList(), prevKey = null, nextKey = null)
                    }
                    return LoadResult.Page(
                        data = data.second,
                        prevKey = null,
                        nextKey = data.second.last().notificationId
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return LoadResult.Error(e)
        }
    }
}