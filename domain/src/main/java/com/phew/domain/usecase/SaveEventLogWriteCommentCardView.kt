package com.phew.domain.usecase


import com.phew.domain.repository.event.EventRepository
import javax.inject.Inject

class SaveEventLogWriteCommentCardView @Inject constructor(private val repository: EventRepository) {
    suspend fun logBackgroundChange() = repository.logWriteCommentCardBackgroundChange()
    suspend fun logBackHandler() = repository.logWriteBackCommentCard()
    suspend fun logWriteTagClickEnter() = repository.logWriteTagWriteFinishWithEnter()
}