package com.phew.domain.usecase

import com.phew.domain.repository.event.EventRepository
import javax.inject.Inject

class SaveEventLogTagView @Inject constructor(private val eventRepository: EventRepository) {
    suspend fun logClickSearchView() = eventRepository.logTagClickSearchView()
    suspend fun logSelectPopularTag() = eventRepository.logTagClickPopularTag()
}