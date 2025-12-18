package com.phew.domain.usecase

import com.phew.core_common.MoveDetail
import com.phew.domain.repository.event.EventRepository
import javax.inject.Inject

class SaveEventLogWriteCardView @Inject constructor(private val repository: EventRepository) {
    suspend fun logBottomWriteClick() = repository.logWriteBottomAddCard()
    suspend fun logWriteTagClickEnter() = repository.logWriteTagWriteFinishWithEnter()
    suspend fun logChangeBackgroundCategory() = repository.logWriteCountBackgroundChange()
    suspend fun logWriteCardFinish(isSharedDistance: Boolean) {
        repository.logWriteCardClickFinishButton()
        if (!isSharedDistance) {
            repository.logWriteDistanceSharedOff()
        }
    }

    suspend fun logBackHandler() = repository.logWriteBackToFeedCard()
}