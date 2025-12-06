package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_NETWORK
import com.phew.domain.repository.network.CardFeedRepository
import javax.inject.Inject

class CheckCardAlreadyDelete @Inject constructor(private val repository: CardFeedRepository) {
    data class Param(
        val cardId: Long,
    )

    suspend operator fun invoke(param: Param): DomainResult<Boolean, String> {
        return when (val request = repository.requestCheckCardDelete(cardId = param.cardId)) {
            is DataResult.Fail -> {
                DomainResult.Failure(request.message ?: ERROR_NETWORK)
            }

            is DataResult.Success -> {
                val result = request.data
                DomainResult.Success(result)
            }
        }
    }
}