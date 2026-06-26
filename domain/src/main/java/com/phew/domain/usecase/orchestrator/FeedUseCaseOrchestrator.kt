package com.phew.domain.usecase.orchestrator

import androidx.paging.PagingData
import com.phew.core_common.DomainResult
import com.phew.domain.dto.CardArticle
import com.phew.domain.dto.FeedCardType
import com.phew.domain.dto.Location
import com.phew.domain.dto.Notice
import com.phew.domain.dto.NoticeSource
import com.phew.domain.dto.Notification
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.FeedPagingFactory
import com.phew.domain.repository.FeedPagingQuery
import com.phew.domain.usecase.CheckCardAlreadyDelete
import com.phew.domain.usecase.CheckLocationPermission
import com.phew.domain.usecase.GetCardArticle
import com.phew.domain.usecase.GetFeedNotification
import com.phew.domain.usecase.GetNotification
import com.phew.domain.usecase.GetReadNotification
import com.phew.domain.usecase.GetUnReadNotification
import com.phew.domain.usecase.LikeCard
import com.phew.domain.usecase.SaveEventLogFeedView
import com.phew.domain.usecase.SetReadActivateNotify
import com.phew.domain.usecase.UnlikeCard
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FeedUseCaseOrchestrator @Inject constructor(
    private val checkLocationPermission: CheckLocationPermission,
    private val getNotificationPage: GetNotification,
    private val getUnReadNotification: GetUnReadNotification,
    private val getReadNotification: GetReadNotification,
    private val feedPagingFactory: FeedPagingFactory,
    private val deviceRepository: DeviceRepository,
    private val getFeedNotification: GetFeedNotification,
    private val setReadActivateNotify: SetReadActivateNotify,
    private val checkCardAlreadyDelete: CheckCardAlreadyDelete,
    private val eventLog: SaveEventLogFeedView,
    private val getCardArticle: GetCardArticle,
    private val likeCard: LikeCard,
    private val unlikeCard: UnlikeCard,
) {
    fun hasLocationPermission(): Boolean = checkLocationPermission()

    suspend fun getLocation(): Location = deviceRepository.requestLocation()

    fun noticePage(source: NoticeSource): Flow<PagingData<Notice>> = getNotificationPage(source)

    fun unreadNotificationPage(): Flow<PagingData<Notification>> = getUnReadNotification()

    fun readNotificationPage(): Flow<PagingData<Notification>> = getReadNotification()

    fun feedPaging(query: FeedPagingQuery): Flow<PagingData<FeedCardType>> =
        feedPagingFactory.create(query)

    suspend fun getFeedNotification(source: NoticeSource): DomainResult<List<Notice>, String> =
        getFeedNotification.invoke(source)

    suspend fun markNotificationsRead(notifyIds: List<Long>): DomainResult<Unit, String> =
        setReadActivateNotify(SetReadActivateNotify.Param(notifyId = notifyIds))

    suspend fun checkCardDeleted(cardId: Long): DomainResult<Boolean, String> =
        checkCardAlreadyDelete(CheckCardAlreadyDelete.Param(cardId = cardId))

    suspend fun moveToTop() {
        eventLog.moveToTop()
    }

    suspend fun moveToCardDetail(isEventCard: Boolean) {
        if (isEventCard) {
            eventLog.moveToCardDetailWhenEventCard()
        } else {
            eventLog.moveToCardDetail()
        }
    }

    suspend fun getCardArticle(): DomainResult<CardArticle, String> = getCardArticle.invoke()

    suspend fun setCardLike(cardId: Long, shouldLike: Boolean): DomainResult<Unit, String> =
        if (shouldLike) likeCard(cardId) else unlikeCard(cardId)
}
