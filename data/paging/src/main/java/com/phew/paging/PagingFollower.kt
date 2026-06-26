package com.phew.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
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
            request.fold(
                onSuccess = { data ->
                    if (data.second.isEmpty()) {
                        return LoadResult.Page(
                            data = emptyList(),
                            prevKey = null,
                            nextKey = null
                        )
                    }
                    if (data.first == HTTP_NO_MORE_CONTENT) {
                        return LoadResult.Page(
                            data = data.second,
                            prevKey = null,
                            nextKey = null
                        )
                    }
                    return LoadResult.Page(
                        data = data.second,
                        prevKey = null,
                        nextKey = data.second.last().followId
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
