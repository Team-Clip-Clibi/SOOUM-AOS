package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.domain.model.TagInfoList
import com.phew.domain.repository.network.TagRepository
import javax.inject.Inject

class GetRelatedTags @Inject constructor(
    private val repository: TagRepository
) {
    data class Param(
        val resultCnt: Long,
        val tag: String
    )

    suspend operator fun invoke(param: Param): DataResult<TagInfoList> {
        return when (val result = repository.getRelatedTags(param.resultCnt, param.tag)) {
            is DataResult.Success -> {
                // Handle case when no tags are found (empty list)
                if (result.data.tagInfos.isEmpty()) {
                    DataResult.Fail(
                        message = "No Content",
                        code = HTTP_NO_MORE_CONTENT
                    )
                } else {
                    DataResult.Success(result.data)
                }
            }
            is DataResult.Fail -> mapFailure(result)
        }
    }

    private fun mapFailure(result: DataResult.Fail): DataResult<TagInfoList> {
        return when (result.code) {
            HTTP_NO_MORE_CONTENT -> DataResult.Fail(
                message = "No Content",
                code = HTTP_NO_MORE_CONTENT
            )
            HTTP_INVALID_TOKEN -> DataResult.Fail(code = result.code, message = ERROR_LOGOUT)
            else -> DataResult.Fail(code = result.code, message = ERROR_NETWORK)
        }
    }
}