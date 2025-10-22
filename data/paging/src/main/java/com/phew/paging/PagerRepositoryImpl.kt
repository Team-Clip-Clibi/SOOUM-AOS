package com.phew.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.phew.domain.dto.CardComment
import com.phew.domain.dto.Notice
import com.phew.domain.dto.Notification
import com.phew.domain.repository.PagerRepository
import com.phew.domain.repository.network.CardDetailRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PagerRepositoryImpl @Inject constructor(
    private val pagingNotifyProvider: javax.inject.Provider<PagingNotify>,
    private val pagingNotificationProvider: javax.inject.Provider<PagingNotificationUnRead>,
    private val pagingUnReadNotificationProvider: javax.inject.Provider<PagingNotificationRead>,
    private val cardDetailRepository: CardDetailRepository
) : PagerRepository {
    override fun noticePageStream(): Flow<PagingData<Notice>> =
        Pager(PagingConfig(pageSize = 30)) { pagingNotifyProvider.get() }.flow

    override fun notificationUnRead(): Flow<PagingData<Notification>> = Pager(
        PagingConfig(pageSize = 30)
    ) { pagingNotificationProvider.get() }.flow

    override fun notificationRead(): Flow<PagingData<Notification>> = Pager(
        PagingConfig(pageSize = 30)
    ) { pagingUnReadNotificationProvider.get() }.flow

    override fun cardComments(
        cardId: Int,
        latitude: Double?,
        longitude: Double?
    ): Flow<PagingData<CardComment>> =
        Pager(
            PagingConfig(pageSize = 30)
        ) { PagingCardComments(cardDetailRepository, cardId, latitude, longitude) }.flow
}
