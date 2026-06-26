package com.phew.domain.usecase

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.mapFailureMessage
import com.phew.domain.repository.network.CardDetailRepository
import javax.inject.Inject

class UnblockMember @Inject constructor(
    private val repository: CardDetailRepository
) {
    data class Param(
        val toMemberId: Long
    )

    suspend operator fun invoke(param: Param): Result<Unit> {
        return repository.unblockMember(param.toMemberId).mapFailureMessage { code, message ->
            when (code) {
                HTTP_INVALID_TOKEN -> ERROR_LOGOUT
                APP_ERROR_CODE -> message.ifBlank { ERROR_FAIL_JOB }
                else -> ERROR_NETWORK
            }
        }
    }
}
