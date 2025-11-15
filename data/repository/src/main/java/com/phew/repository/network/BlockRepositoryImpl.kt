package com.phew.repository.network


import com.phew.core_common.DataResult
import com.phew.core_common.log.SooumLog
import com.phew.domain.model.BlockMember
import com.phew.domain.repository.network.BlockRepository
import com.phew.network.retrofit.BlockHttp
import com.phew.repository.mapper.apiCall
import com.phew.repository.mapper.toDomain
import javax.inject.Inject

class BlockRepositoryImpl @Inject constructor(
    private val blockHttp: BlockHttp
) : BlockRepository {
    override suspend fun getBlockList(): Result<List<BlockMember>> {
        SooumLog.d(TAG, "getBlockList")
        
        return when (val result = apiCall(
            apiCall = { blockHttp.getBlockList() },
            mapper = { it.map { dto -> dto.toDomain() } }
        )) {
            is DataResult.Success -> Result.success(result.data)
            is DataResult.Fail -> Result.failure(
                result.throwable ?: Exception("Failed to get block list: ${result.message}")
            )
        }
    }

    override suspend fun getBlockListPaging(lastBlockId: Long): DataResult<List<BlockMember>> {
        SooumLog.d(TAG, "getBlockListPaging - lastBlockId: $lastBlockId")
        return apiCall(
            apiCall = { blockHttp.getBlockListPaging(lastBlockId) },
            mapper = { it.map { dto -> dto.toDomain() } }
        )
    }
}

private const val TAG = "BlockRepository"