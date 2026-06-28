package com.phew.domain.orchestrator

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.paging.PagingData
import androidx.core.content.ContextCompat
import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.ERROR_ALREADY_CARD_DELETE
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.ERROR_NO_DATA
import com.phew.core_common.HTTP_CARD_ALREADY_DELETE
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.core_common.HTTP_NOT_FOUND
import com.phew.core_common.HTTP_SUCCESS
import com.phew.core_common.exception.asSooumException
import com.phew.core_common.mapFailureMessage
import com.phew.core_common.resultFailure
import com.phew.domain.dto.CardArticle
import com.phew.domain.dto.FeedCardType
import com.phew.domain.dto.Location
import com.phew.domain.dto.Notice
import com.phew.domain.dto.NoticeSource
import com.phew.domain.dto.Notification
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.FeedPagingFactory
import com.phew.domain.repository.FeedPagingQuery
import com.phew.domain.repository.PagerRepository
import com.phew.domain.repository.event.EventRepository
import com.phew.domain.repository.network.CardDetailRepository
import com.phew.domain.repository.network.CardFeedRepository
import com.phew.domain.repository.network.NotifyRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

/**
 * 피드 화면에서 피드/알림 페이징, 위치 권한, 카드 상태 확인, 좋아요, 이벤트 로그 흐름을 조율합니다.
 */
class FeedUseCaseOrchestrator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pagerRepository: PagerRepository,
    private val feedPagingFactory: FeedPagingFactory,
    private val deviceRepository: DeviceRepository,
    private val notifyRepository: NotifyRepository,
    private val cardFeedRepository: CardFeedRepository,
    private val cardDetailRepository: CardDetailRepository,
    private val eventRepository: EventRepository,
) {
    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    suspend fun getLocation(): Location = deviceRepository.requestLocation()

    fun noticePage(source: NoticeSource): Flow<PagingData<Notice>> =
        pagerRepository.noticePageStream(source)

    fun unreadNotificationPage(): Flow<PagingData<Notification>> =
        pagerRepository.notificationUnRead()

    fun readNotificationPage(): Flow<PagingData<Notification>> =
        pagerRepository.notificationRead()

    fun feedPaging(query: FeedPagingQuery): Flow<PagingData<FeedCardType>> =
        feedPagingFactory.create(query)

    suspend fun getFeedNotification(source: NoticeSource): Result<List<Notice>> =
        notifyRepository.requestNotice(pageSize = FEED_NOTICE_PAGE_SIZE, source = source).fold(
            onSuccess = { request ->
                Result.success(request.second.sortedByDescending { data -> data.id }.take(FEED_NOTICE_PAGE_SIZE))
            },
            onFailure = { throwable ->
                val exception = throwable.asSooumException()
                resultFailure(
                    code = exception.code,
                    message = if (exception.code == HTTP_INVALID_TOKEN) ERROR_LOGOUT else ERROR_NETWORK,
                    throwable = throwable,
                )
            },
        )

    suspend fun markNotificationsRead(notifyIds: List<Long>): Result<Unit> =
        if (notifyIds.isEmpty()) {
            Log.e(TAG, "notify data is Empty")
            Result.success(Unit)
        } else {
            supervisorScope {
                val results = notifyIds.map { id ->
                    async {
                        notifyRepository.requestReadNotify(notifyId = id) == HTTP_SUCCESS
                    }
                }.awaitAll()
                if (results.contains(true)) {
                    Result.success(Unit)
                } else {
                    resultFailure(ERROR_NETWORK)
                }
            }
        }

    suspend fun checkCardDeleted(cardId: Long): Result<Boolean> =
        cardFeedRepository.requestCheckCardDelete(cardId = cardId)
            .mapFailureMessage { _, message -> message.ifBlank { ERROR_NETWORK } }

    suspend fun moveToTop() {
        eventRepository.logFeedMoveToTop()
    }

    suspend fun moveToCardDetail(isEventCard: Boolean) {
        if (isEventCard) {
            eventRepository.logFeedClickEventCard()
        } else {
            eventRepository.logFeedMoveToDetail()
        }
    }

    suspend fun getCardArticle(): Result<CardArticle> =
        cardFeedRepository.requestCardArticle().fold(
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

    suspend fun setCardLike(cardId: Long, shouldLike: Boolean): Result<Unit> =
        (if (shouldLike) {
            cardDetailRepository.likeCard(cardId)
        } else {
            cardDetailRepository.unlikeCard(cardId)
        }).mapFailureMessage { code, message ->
            when (code) {
                APP_ERROR_CODE -> message.ifBlank { ERROR_FAIL_JOB }
                HTTP_CARD_ALREADY_DELETE -> ERROR_ALREADY_CARD_DELETE
                HTTP_NOT_FOUND -> ERROR_FAIL_JOB
                else -> ERROR_FAIL_JOB
            }
        }

    private companion object {
        private const val TAG = "FeedUseCaseOrchestrator"
        private const val FEED_NOTICE_PAGE_SIZE = 3
    }
}
