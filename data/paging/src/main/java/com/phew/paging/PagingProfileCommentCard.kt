package com.phew.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.domain.dto.ProfileCard
import com.phew.domain.repository.network.ProfileRepository

class PagingProfileCommentCard(private val repository: ProfileRepository) :
    PagingSource<Long, ProfileCard>() {
    override fun getRefreshKey(state: PagingState<Long, ProfileCard>): Long? {
        return null
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, ProfileCard> {
        val cardId = params.key
        try {
            val request = if (cardId == null) {
                repository.requestProfileCommentCard()
            } else {
                repository.requestProfileCommentCardNext(cardId = cardId)
            }
            request.fold(
                onSuccess = { data ->
                    when {
                        data.second.isEmpty() || data.first == HTTP_NO_MORE_CONTENT || data.second.isNotEmpty() && data.second.last().cardId == params.key -> {
                            return LoadResult.Page(
                                data = emptyList(), prevKey = null, nextKey = null
                            )
                        }


                        else -> {
                            return LoadResult.Page(
                                data = data.second,
                                prevKey = null,
                                nextKey = data.second.last().cardId
                            )
                        }
                    }
                },
                onFailure = { return LoadResult.Error(it) },
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return LoadResult.Error(e)
        }
    }
}
