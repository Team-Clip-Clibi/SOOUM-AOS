package com.phew.domain.orchestrator

import androidx.paging.PagingData
import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.CardDetailTrace
import com.phew.core_common.ERROR_ALREADY_CARD_DELETE
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_CARD_ALREADY_DELETE
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.core_common.HTTP_NOT_FOUND
import com.phew.core_common.MoveDetail
import com.phew.core_common.exception.asSooumException
import com.phew.core_common.log.SooumLog
import com.phew.core_common.mapFailureMessage
import com.phew.core_common.resultFailure
import com.phew.domain.dto.CardComment
import com.phew.domain.dto.CardDetail
import com.phew.domain.dto.Poll
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.PagerRepository
import com.phew.domain.repository.event.EventRepository
import com.phew.domain.repository.network.CardDetailRepository
import com.phew.domain.repository.network.CardFeedRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 카드 상세 화면에서 상세 조회, 댓글, 좋아요, 투표, 삭제, 차단, 상세 진입 로그 흐름을 조율합니다.
 */
class CardDetailUseCaseOrchestrator @Inject constructor(
    private val cardDetailRepository: CardDetailRepository,
    private val cardFeedRepository: CardFeedRepository,
    private val pagerRepository: PagerRepository,
    private val deviceRepository: DeviceRepository,
    private val eventRepository: EventRepository,
) {
    suspend fun cardDetail(cardId: Long): Result<CardDetail> {
        val (latitude, longitude) = currentLocationOrNull()
        return cardDetailRepository.getCardDetail(cardId, latitude, longitude)
            .mapFailureMessage { code, message ->
                when (code) {
                    HTTP_INVALID_TOKEN -> ERROR_LOGOUT
                    APP_ERROR_CODE -> message.ifBlank { ERROR_FAIL_JOB }
                    HTTP_CARD_ALREADY_DELETE -> ERROR_ALREADY_CARD_DELETE
                    else -> ERROR_NETWORK
                }
            }
    }

    suspend fun cardComments(cardId: Long): Result<List<CardComment>> {
        SooumLog.d(TAG, "cardComments() start cardId: $cardId")
        val (latitude, longitude) = currentLocationOrNull()
        return cardDetailRepository.getCardComments(cardId, latitude, longitude).fold(
            onSuccess = { Result.success(it) },
            onFailure = { throwable ->
                val exception = throwable.asSooumException()
                if (exception.code == HTTP_NO_MORE_CONTENT) {
                    Result.success(emptyList())
                } else {
                    mapCommentsFailure(exception.code, exception.message, throwable)
                }
            },
        )
    }

    suspend fun cardCommentsPaging(cardId: Long): Flow<PagingData<CardComment>> {
        val (latitude, longitude) = currentLocationOrNull()
        return pagerRepository.cardComments(
            cardId = cardId,
            latitude = latitude,
            longitude = longitude,
        )
    }

    suspend fun setCardLike(cardId: Long, shouldLike: Boolean): Result<Unit> =
        (if (shouldLike) {
            cardDetailRepository.likeCard(cardId)
        } else {
            cardDetailRepository.unlikeCard(cardId)
        }).mapCardMutationFailure()

    suspend fun createPollVote(pollOptionId: Long): Result<Poll> =
        cardDetailRepository.createPollVote(pollOptionId).mapFailureMessage { code, message ->
            when (code) {
                HTTP_INVALID_TOKEN -> ERROR_NETWORK
                APP_ERROR_CODE -> message.ifBlank { ERROR_FAIL_JOB }
                HTTP_CARD_ALREADY_DELETE -> ERROR_ALREADY_CARD_DELETE
                else -> ERROR_NETWORK
            }
        }

    suspend fun deletePollVote(pollOptionId: Long): Result<Unit> =
        cardDetailRepository.deletePollVote(pollOptionId).mapFailureMessage { code, message ->
            when (code) {
                HTTP_INVALID_TOKEN -> ERROR_NETWORK
                APP_ERROR_CODE -> message.ifBlank { ERROR_FAIL_JOB }
                HTTP_CARD_ALREADY_DELETE -> ERROR_ALREADY_CARD_DELETE
                else -> ERROR_NETWORK
            }
        }

    suspend fun deleteCard(cardId: Long): Result<Unit> =
        cardDetailRepository.deleteCard(cardId).mapFailureMessage { code, message ->
            when (code) {
                HTTP_INVALID_TOKEN -> ERROR_LOGOUT
                APP_ERROR_CODE -> message.ifBlank { ERROR_FAIL_JOB }
                else -> ERROR_NETWORK
            }
        }

    suspend fun blockMember(toMemberId: Long): Result<Unit> =
        cardDetailRepository.blockMember(toMemberId).mapMemberMutationFailure()

    suspend fun unblockMember(toMemberId: Long): Result<Unit> =
        cardDetailRepository.unblockMember(toMemberId).mapMemberMutationFailure()

    suspend fun checkCardDeleted(cardId: Long): Result<Boolean> =
        cardFeedRepository.requestCheckCardDelete(cardId = cardId)
            .mapFailureMessage { _, message -> message.ifBlank { ERROR_NETWORK } }

    suspend fun moveToCommentCard(event: MoveDetail, isEventCard: Boolean) {
        eventRepository.logDetailWriteCommentCard()
        when {
            event == MoveDetail.FLOAT && isEventCard -> eventRepository.logDetailWriteCardWhenBackgroundEventCard()
            event == MoveDetail.FLOAT -> eventRepository.logDetailWriteCommentCardFloatButton()
            event == MoveDetail.IMAGE -> eventRepository.logDetailWriteCommentCardImage()
        }
    }

    suspend fun moveToTagView() {
        eventRepository.logDetailTagClick()
    }

    suspend fun tracePreviousView(view: CardDetailTrace) {
        if (view == CardDetailTrace.NONE) return
        eventRepository.traceWhereComeFromCardDetail(view.value)
    }

    private suspend fun currentLocationOrNull(): Pair<Double?, Double?> {
        val hasPermission = deviceRepository.getLocationPermission()
        return if (hasPermission) {
            val location = deviceRepository.requestLocation()
            location.latitude to location.longitude
        } else {
            null to null
        }
    }

    private fun Result<Unit>.mapCardMutationFailure(): Result<Unit> =
        mapFailureMessage { code, message ->
            when (code) {
                APP_ERROR_CODE -> message.ifBlank { ERROR_FAIL_JOB }
                HTTP_CARD_ALREADY_DELETE -> ERROR_ALREADY_CARD_DELETE
                HTTP_NOT_FOUND -> ERROR_FAIL_JOB
                else -> ERROR_FAIL_JOB
            }
        }

    private fun Result<Unit>.mapMemberMutationFailure(): Result<Unit> =
        mapFailureMessage { code, message ->
            when (code) {
                HTTP_INVALID_TOKEN -> ERROR_LOGOUT
                APP_ERROR_CODE -> message.ifBlank { ERROR_FAIL_JOB }
                else -> ERROR_NETWORK
            }
        }

    private fun mapCommentsFailure(code: Int, message: String, throwable: Throwable): Result<List<CardComment>> =
        when (code) {
            HTTP_INVALID_TOKEN -> resultFailure(message = ERROR_LOGOUT, throwable = throwable)
            APP_ERROR_CODE -> resultFailure(message = message.ifBlank { ERROR_FAIL_JOB }, throwable = throwable)
            else -> resultFailure(message = ERROR_NETWORK, throwable = throwable)
        }

    private companion object {
        private const val TAG = "CardDetailUseCaseOrchestrator"
    }
}
