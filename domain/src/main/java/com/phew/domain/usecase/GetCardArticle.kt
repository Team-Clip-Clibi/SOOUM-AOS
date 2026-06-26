package com.phew.domain.usecase

import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.ERROR_NO_DATA
import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.core_common.exception.asSooumException
import com.phew.core_common.resultFailure
import com.phew.domain.dto.CardArticle
import com.phew.domain.repository.network.CardFeedRepository
import javax.inject.Inject

class GetCardArticle @Inject constructor(private val repository: CardFeedRepository) {
    suspend operator fun invoke(): Result<CardArticle> {
        return repository.requestCardArticle().fold(
            onSuccess = { Result.success(it) },
            onFailure = { throwable ->
                val exception = throwable.asSooumException()
                if (exception.code == HTTP_NO_MORE_CONTENT) {
                    resultFailure(message = ERROR_NO_DATA)
                } else {
                    resultFailure(message = exception.message.ifBlank { ERROR_NETWORK })
                }
            },
        )
    }
}
