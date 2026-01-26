package com.phew.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phew.core_common.DataResult
import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.domain.dto.FollowData
import com.phew.domain.repository.network.ProfileRepository

class PagingFollowing(
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
                repository.requestFollowing(profileId = profileId)
            } else {
                repository.requestFollowingNext(profileId = profileId, lastId = userId)
            }
            when (request) {
                is DataResult.Fail -> return LoadResult.Error(Throwable(request.message))
                is DataResult.Success -> {
                    val (status, dataList) = request.data
                    val nextKey = if (dataList.isEmpty() || status == HTTP_NO_MORE_CONTENT) {
                        null
                    } else {
                        dataList.last().followId
                    }
                    return LoadResult.Page(
                        data = request.data.second,
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