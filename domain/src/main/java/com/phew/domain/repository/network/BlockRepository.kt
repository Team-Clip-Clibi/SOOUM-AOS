package com.phew.domain.repository.network

import com.phew.core_common.DataResult
import com.phew.domain.model.BlockMember

interface BlockRepository {
    suspend fun getBlockList(): Result<List<BlockMember>>
    suspend fun getBlockListPaging(lastBlockId: Long): DataResult<List<BlockMember>>
}