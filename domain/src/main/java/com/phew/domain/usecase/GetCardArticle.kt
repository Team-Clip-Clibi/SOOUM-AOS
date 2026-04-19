package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.ERROR_NO_DATA
import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.domain.dto.CardArticle
import com.phew.domain.repository.network.CardFeedRepository
import javax.inject.Inject

class GetCardArticle @Inject constructor(private val repository: CardFeedRepository) {
    suspend operator fun invoke(): DomainResult<CardArticle, String> {
        return when (val request = repository.requestCardArticle()) {
            is DataResult.Fail -> {
                if (request.code == HTTP_NO_MORE_CONTENT) {
                    return DomainResult.Failure(ERROR_NO_DATA)
                }
                return DomainResult.Failure(request.message ?: ERROR_NETWORK)
            }

            is DataResult.Success -> {
                DomainResult.Success(request.data)
            }
        }
    }
}