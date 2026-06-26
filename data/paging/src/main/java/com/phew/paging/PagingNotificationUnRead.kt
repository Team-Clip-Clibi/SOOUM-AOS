package com.phew.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phew.domain.dto.Notification
import com.phew.domain.repository.network.NotifyRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class PagingNotificationUnRead @Inject constructor(
    private val notifyRepository: NotifyRepository
) : PagingSource<Long, Notification>() {

    override fun getRefreshKey(state: PagingState<Long, Notification>): Long? {
        return null
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Notification> {
        try {
            val key = params.key ?: -1
            val result = if (key == -1L) {
                notifyRepository.requestNotificationUnRead()
            } else {
                notifyRepository.requestNotificationUnReadPatch(lastId = key)
            }
            result.fold(
                onSuccess = { data ->
                    delay(2000L)
                    val unReadData = data.second
                    val currentKey = params.key ?: -1L
                    val unRead = if(currentKey != -1L){
                        unReadData.filter { data -> data.notificationId < currentKey }
                    } else {
                        unReadData
                    }
                    if(unRead.isEmpty()){
                        return LoadResult.Page(
                            data = emptyList(),
                            prevKey = null,
                            nextKey = null
                        )
                    }
                    return LoadResult.Page(
                        data = unRead,
                        prevKey = null,
                        nextKey = unRead.last().notificationId
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
