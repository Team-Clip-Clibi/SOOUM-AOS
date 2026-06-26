package com.phew.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phew.domain.dto.Notification
import com.phew.domain.repository.network.NotifyRepository
import kotlinx.coroutines.delay
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
            result.fold(
                onSuccess = { data ->
                    val readData = data.second
                    val currentKey = params.key ?: -1L
                    val read = if (currentKey != -1L) {
                        readData.filter { data -> data.notificationId < currentKey }
                    } else {
                        readData
                    }
                    delay(2000L)
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
                },
                onFailure = { return LoadResult.Error(it) },
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return LoadResult.Error(e)
        }
    }
}
