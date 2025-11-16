package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.domain.model.TagInfoList
import com.phew.domain.repository.network.TagRepository
import javax.inject.Inject

class GetTagRank @Inject constructor(
    private val repository: TagRepository
) {
    suspend operator fun invoke(): DataResult<TagInfoList> {
        return when (val result = repository.getTagRank()) {
            is DataResult.Success -> DataResult.Success(result.data)
            is DataResult.Fail -> mapFailure(result)
        }
    }

    private fun mapFailure(result: DataResult.Fail): DataResult<TagInfoList> {
        return when (result.code) {
            HTTP_INVALID_TOKEN -> DataResult.Fail(code = result.code, message = ERROR_LOGOUT)
            else -> DataResult.Fail(code = result.code, message = ERROR_NETWORK)
        }
    }
}