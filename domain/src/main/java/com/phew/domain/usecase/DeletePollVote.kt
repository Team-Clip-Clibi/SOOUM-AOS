package com.phew.domain.usecase

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.ERROR_ALREADY_CARD_DELETE
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_CARD_ALREADY_DELETE
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.mapFailureMessage
import com.phew.domain.repository.network.CardDetailRepository
import javax.inject.Inject

class DeletePollVote @Inject constructor(
    private val repository: CardDetailRepository
) {
    data class Param(
        val pollOptionId: Long
    )

    suspend operator fun invoke(param: Param): Result<Unit> {
        return repository.deletePollVote(param.pollOptionId).mapFailureMessage { code, message ->
            when (code) {
                HTTP_INVALID_TOKEN -> ERROR_NETWORK
                APP_ERROR_CODE -> message.ifBlank { ERROR_FAIL_JOB }
                HTTP_CARD_ALREADY_DELETE -> ERROR_ALREADY_CARD_DELETE
                else -> ERROR_NETWORK
            }
        }
    }
}
