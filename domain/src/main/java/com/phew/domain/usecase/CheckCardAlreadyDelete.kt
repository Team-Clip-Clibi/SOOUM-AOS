package com.phew.domain.usecase

import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.mapFailureMessage
import com.phew.domain.repository.network.CardFeedRepository
import javax.inject.Inject

class CheckCardAlreadyDelete @Inject constructor(private val repository: CardFeedRepository) {
    data class Param(
        val cardId: Long,
    )

    suspend operator fun invoke(param: Param): Result<Boolean> {
        return repository.requestCheckCardDelete(cardId = param.cardId)
            .mapFailureMessage { _, message -> message.ifBlank { ERROR_NETWORK } }
    }
}
