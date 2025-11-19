package com.phew.domain.usecase


import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_BAD_REQUEST
import com.phew.core_common.HTTP_TOKEN_ERROR
import com.phew.core_common.WITHDRAWAL_USER
import com.phew.domain.model.TagInfo
import com.phew.domain.model.TagInfoList
import com.phew.domain.repository.network.TagRepository
import javax.inject.Inject

class GetTagRank @Inject constructor(
    private val repository: TagRepository,
) {
    suspend operator fun invoke(): DomainResult<List<TagInfo>, String> {
        return when (val result = repository.getTagRank()) {
            is DataResult.Success -> mapSuccess(result.data)
            is DataResult.Fail -> mapFailure(result)
        }
    }

    private fun mapSuccess(result: TagInfoList): DomainResult.Success<List<TagInfo>> {
        return DomainResult.Success(
            result.tagInfos.filter { data -> data.usageCnt > 0 }
                .sortedByDescending { data -> data.usageCnt }
        )
    }

    private fun mapFailure(result: DataResult.Fail): DomainResult.Failure<String> {
        return when (result.code) {
            HTTP_INVALID_TOKEN, WITHDRAWAL_USER, HTTP_TOKEN_ERROR -> DomainResult.Failure(
                ERROR_LOGOUT
            )
            HTTP_BAD_REQUEST -> DomainResult.Failure(ERROR_NETWORK)
            else -> DomainResult.Failure(ERROR_FAIL_JOB)
        }
    }
}