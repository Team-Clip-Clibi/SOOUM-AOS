package com.phew.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phew.core_common.DataResult
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.domain.dto.Notification
import com.phew.domain.repository.network.NotifyRepository
import javax.inject.Inject

class PagingNotificationUnRead @Inject constructor(
    private val notifyRepository: NotifyRepository
) : PagingSource<Long, Notification>() {

    override fun getRefreshKey(state: PagingState<Long, Notification>): Long? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey
                ?: state.closestPageToPosition(anchorPosition)?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Notification> {
        try {
            val key = params.key ?: -1
            val result = if (key == -1L) {
                notifyRepository.requestNotificationUnRead()
            } else {
                notifyRepository.requestNotificationUnReadPatch(lastId = key)
            }
            when (result) {
                is DataResult.Fail -> {
                    if (result.code != HTTP_INVALID_TOKEN) {
                        return LoadResult.Error(Throwable(ERROR_NETWORK))
                    }
                    return LoadResult.Page(data = emptyList(), prevKey = null, nextKey = null)
                }

                is DataResult.Success -> {
                    val originalNotificationList = result.data.second.sortedBy { data -> data.notificationId }
                    val notificationList = if(key != -1L){
                        originalNotificationList.filter { data -> data.notificationId != key }
                    }else{
                        originalNotificationList
                    }
                    val lastItemId = notificationList.lastOrNull()?.notificationId
                    val nextKey = if (notificationList.isEmpty() || lastItemId == key) {
                        null
                    } else {
                        notificationList.last().notificationId
                    }
                    return LoadResult.Page(
                        data = notificationList,
                        prevKey = null,
                        nextKey = nextKey
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return LoadResult.Error(e)
        }
    }
}