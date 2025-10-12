package com.phew.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phew.core_common.DataResult
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.domain.dto.Notification
import com.phew.domain.repository.network.NotifyRepository
import java.io.IOException
import javax.inject.Inject

class PagingNotificationRead @Inject constructor(
    private val notifyRepository: NotifyRepository
) : PagingSource<Long, Notification>() {

    override fun getRefreshKey(state: PagingState<Long, Notification>): Long? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey
                ?: state.closestPageToPosition(anchorPosition)?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Notification> {
        val key = params.key ?: -1L

        return try {
            val result = if (key == -1L) {
                notifyRepository.requestNotificationRead()
            } else {
                notifyRepository.requestNotificationReadPatch(lastId = key)
            }

            when (result) {
                is DataResult.Success -> {
                    val notificationList = result.data.second
                    val isLastPage = notificationList.isEmpty()

                    val nextKey = if (isLastPage) {
                        null
                    } else {
                        notificationList.last().notificationId
                    }

                    LoadResult.Page(
                        data = notificationList,
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