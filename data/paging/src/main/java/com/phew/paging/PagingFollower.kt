package com.phew.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phew.core_common.DataResult
import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.domain.dto.FollowData
import com.phew.domain.repository.network.ProfileRepository

class PagingFollower(
    private val repository: ProfileRepository,
    private val profileId: Long,
) : PagingSource<Long, FollowData>() {
    override fun getRefreshKey(state: PagingState<Long, FollowData>): Long? {
        return null
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, FollowData> {
        val userId = params.key
        try {
            val request = if (userId == null) {
                repository.requestFollower(profileId = profileId)
            } else {
                repository.requestFollowerNext(profileId = profileId, lastId = userId)
            }
            when (request) {
                is DataResult.Fail -> return LoadResult.Error(Throwable(request.message))
                is DataResult.Success -> {
                    if (request.data.second.isEmpty()) {
                        return LoadResult.Page(
                            data = emptyList(),
                            prevKey = null,
                            nextKey = null
                        )
                    }
                    val sortedList = request.data.second.sortedBy { data -> data.memberId }
                    if (request.data.first == HTTP_NO_MORE_CONTENT) {
                        return LoadResult.Page(
                            data = sortedList,
                            prevKey = null,
                            nextKey = null
                        )
                    }
                    return LoadResult.Page(
                        data = sortedList,
                        prevKey = null,
                        nextKey = request.data.second.last().memberId
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return LoadResult.Error(e)
        }
    }
}