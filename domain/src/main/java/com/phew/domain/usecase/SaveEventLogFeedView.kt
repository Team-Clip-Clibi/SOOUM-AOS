package com.phew.domain.usecase

import com.phew.domain.repository.event.EventRepository
import javax.inject.Inject

class SaveEventLogFeedView @Inject constructor(private val eventRepository: EventRepository) {
    suspend fun moveToTop() = eventRepository.logFeedMoveToTop()
    suspend fun moveToCardDetail() = eventRepository.logFeedMoveToDetail()
    suspend fun moveToCardDetailWhenEventCard() = eventRepository.logFeedClickEventCard()
}