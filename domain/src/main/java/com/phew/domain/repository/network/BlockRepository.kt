package com.phew.domain.repository.network

import com.phew.domain.model.BlockMember

interface BlockRepository {
    suspend fun getBlockList(): Result<List<BlockMember>>
    suspend fun getBlockListPaging(lastBlockId: Long): Result<List<BlockMember>>
}