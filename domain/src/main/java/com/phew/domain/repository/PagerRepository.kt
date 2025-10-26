package com.phew.domain.repository

import androidx.paging.PagingData
import com.phew.domain.dto.CardComment
import com.phew.domain.dto.Notice
import com.phew.domain.dto.Notification
import kotlinx.coroutines.flow.Flow

interface PagerRepository {
    fun noticePageStream(): Flow<PagingData<Notice>>
    fun notificationUnRead(): Flow<PagingData<Notification>>
    fun notificationRead(): Flow<PagingData<Notification>>
    fun cardComments(
        cardId: Long,
        latitude: Double? = null,
        longitude: Double? = null
    ): Flow<PagingData<CardComment>>
}
