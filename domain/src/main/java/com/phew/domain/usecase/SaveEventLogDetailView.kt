package com.phew.domain.usecase

import com.phew.core_common.CardDetailTrace
import com.phew.core_common.MoveDetail
import com.phew.domain.repository.event.EventRepository
import javax.inject.Inject

class SaveEventLogDetailView @Inject constructor(private val repository: EventRepository) {
    suspend fun moveToCommentCard(event: MoveDetail, isEventCard: Boolean) {
        repository.logDetailWriteCommentCard()
        when {
            event == MoveDetail.FLOAT && isEventCard -> repository.logDetailWriteCardWhenBackgroundEventCard()
            event == MoveDetail.FLOAT -> repository.logDetailWriteCommentCardFloatButton()
            event == MoveDetail.IMAGE -> repository.logDetailWriteCommentCardImage()
        }
    }

    suspend fun moveToTagView() = repository.logDetailTagClick()
    suspend fun tracePreviousView(view: CardDetailTrace) {
        if (view == CardDetailTrace.NONE) return
        repository.traceWhereComeFromCardDetail(view.value)
    }
}