package com.phew.domain.usecase

import com.phew.domain.dto.CardDefaultImagesResponse
import com.phew.domain.repository.network.CardFeedRepository
import com.phew.domain.safeUseCase
import javax.inject.Inject

class GetCardDefaultImage @Inject constructor(private val repository: CardFeedRepository) {
    suspend operator fun invoke(): Result<CardDefaultImagesResponse> {
        return safeUseCase(
            apiCall = { repository.requestCardImageDefault() },
            mapper = { result -> result }
        )
    }
}