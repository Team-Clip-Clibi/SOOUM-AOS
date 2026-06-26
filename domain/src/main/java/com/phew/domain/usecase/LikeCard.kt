package com.phew.domain.usecase

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.ERROR_ALREADY_CARD_DELETE
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.HTTP_CARD_ALREADY_DELETE
import com.phew.core_common.HTTP_NOT_FOUND
import com.phew.core_common.mapFailureMessage
import com.phew.domain.repository.network.CardDetailRepository
import javax.inject.Inject

class LikeCard @Inject constructor(
    private val repository: CardDetailRepository
) {
    suspend operator fun invoke(cardId: Long): Result<Unit> {
        return repository.likeCard(cardId).mapFailureMessage { code, message ->
            when (code) {
                APP_ERROR_CODE -> message.ifBlank { ERROR_FAIL_JOB }
                HTTP_CARD_ALREADY_DELETE -> ERROR_ALREADY_CARD_DELETE
                HTTP_NOT_FOUND -> ERROR_FAIL_JOB
                else -> ERROR_FAIL_JOB
            }
        }
    }
}
