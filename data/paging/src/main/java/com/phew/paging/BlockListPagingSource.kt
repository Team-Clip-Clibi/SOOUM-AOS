package com.phew.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phew.core_common.DataResult
import com.phew.domain.model.BlockMember
import com.phew.domain.repository.network.BlockRepository
import java.io.IOException
import javax.inject.Inject

class BlockListPagingSource @Inject constructor(
    private val repository: BlockRepository
) : PagingSource<Long, BlockMember>() {

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, BlockMember> {
        val key = params.key
        return try {
            val users = if (key == null) {
                repository.getBlockList().getOrThrow()
            } else {
                when (val result = repository.getBlockListPaging(key)) {
                    is DataResult.Success -> result.data
                    is DataResult.Fail -> throw result.throwable ?: IOException("Paging Error")
                }
            }
            LoadResult.Page(
                data = users,
                prevKey = null,
                nextKey = users.lastOrNull()?.blockId
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Long, BlockMember>): Long? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey
                ?: state.closestPageToPosition(anchorPosition)?.nextKey
        }
    }
}
