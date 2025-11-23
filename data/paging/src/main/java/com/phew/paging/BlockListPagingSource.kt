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
        val key = params.key ?: -1
        return try {
            when (val result = repository.getBlockListPaging(key)) {
                is DataResult.Success -> {
                    val users = result.data
                    LoadResult.Page(
                        data = users,
                        prevKey = null,
                        nextKey = if (users.isEmpty()) null else users.last().blockId
                    )
                }
                is DataResult.Fail -> {
                    LoadResult.Error(result.throwable ?: IOException("Paging Error"))
                }
            }
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