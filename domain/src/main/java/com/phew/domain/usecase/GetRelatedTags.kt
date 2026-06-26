package com.phew.domain.usecase

import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.mapResult
import com.phew.core_common.resultFailure
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

    suspend operator fun invoke(param: Param): Result<TagInfoList> {
        return repository.getRelatedTags(param.resultCnt, param.tag).mapResult(
            success = { data ->
                if (data.tagInfos.isEmpty()) {
                    return resultFailure(
                        message = "No Content",
                        code = HTTP_NO_MORE_CONTENT
                    )
                }
                data
            },
        ) { code, _ ->
            when (code) {
                HTTP_NO_MORE_CONTENT -> "No Content"
                HTTP_INVALID_TOKEN -> ERROR_LOGOUT
                else -> ERROR_NETWORK
            }
        }
    }
}
