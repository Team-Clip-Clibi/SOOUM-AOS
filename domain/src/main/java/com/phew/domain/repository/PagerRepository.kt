package com.phew.domain.repository

import androidx.paging.PagingData
import com.phew.domain.dto.Notice
import kotlinx.coroutines.flow.Flow

interface PagerRepository {
    fun noticePageStream(): Flow<PagingData<Notice>>
}