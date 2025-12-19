package com.phew.domain.usecase

import com.phew.core_common.MoveDetail
import com.phew.domain.repository.event.EventRepository
import javax.inject.Inject

class SaveEventLogWriteCardView @Inject constructor(private val repository: EventRepository) {
    suspend fun logBottomWriteClick() = repository.logWriteBottomAddCard()
    suspend fun logWriteTagClickEnter() = repository.logWriteTagWriteFinishWithEnter()
    suspend fun logChangeBackgroundCategory() = repository.logWriteCountBackgroundChange()
    suspend fun logBackHandler() = repository.logWriteBackToFeedCard()
    suspend fun logWriteEventCard() = repository.logWriteSelectEventTab()
}