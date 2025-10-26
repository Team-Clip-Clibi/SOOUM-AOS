package com.phew.domain.usecase

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.domain.repository.network.CardDetailRepository
import javax.inject.Inject

class UnblockMember @Inject constructor(
    private val repository: CardDetailRepository
) {
    data class Param(
        val toMemberId: Long
    )

    suspend operator fun invoke(param: Param): DomainResult<Unit, String> {
        return when (val result = repository.unblockMember(param.toMemberId)) {
            is DataResult.Success -> DomainResult.Success(result.data)
            is DataResult.Fail -> mapFailure(result)
        }
    }

    private fun mapFailure(result: DataResult.Fail): DomainResult.Failure<String> {
        return when (result.code) {
            HTTP_INVALID_TOKEN -> DomainResult.Failure(ERROR_LOGOUT)
            APP_ERROR_CODE -> DomainResult.Failure(result.message ?: ERROR_FAIL_JOB)
            else -> DomainResult.Failure(ERROR_NETWORK)
        }
    }
}