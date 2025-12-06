package com.phew.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phew.core_common.DataResult
import com.phew.core_common.ERROR_NETWORK
import com.phew.domain.dto.Notification
import com.phew.domain.repository.network.NotifyRepository
import java.io.IOException
import javax.inject.Inject

class PagingNotificationRead @Inject constructor(
    private val notifyRepository: NotifyRepository
) : PagingSource<Long, Notification>() {

    override fun getRefreshKey(state: PagingState<Long, Notification>): Long? {
        return null
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Notification> {
        val key = params.key ?: -1L
        try {
            val result =
                if (key == -1L) notifyRepository.requestNotificationRead() else notifyRepository.requestNotificationReadPatch(
                    lastId = key
                )
            when (result) {
                is DataResult.Fail -> return LoadResult.Error(IOException(ERROR_NETWORK))
                is DataResult.Success -> {
                    val readData = result.data.second
                    val currentKey = params.key ?: -1L
                    val read = if (currentKey != -1L) {
                        readData.filter { data -> data.notificationId < currentKey }
                    } else {
                        readData
                    }
                    if (read.isEmpty()) return LoadResult.Page(
                        data = emptyList(),
                        prevKey = null,
                        nextKey = null
                    )
                    return LoadResult.Page(
                        data = read,
                        prevKey = null,
                        nextKey = read.last().notificationId
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return LoadResult.Error(e)
        }
    }
}