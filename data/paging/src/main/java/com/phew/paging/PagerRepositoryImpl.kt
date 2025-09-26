package com.phew.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.phew.domain.dto.Notice
import com.phew.domain.repository.PagerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PagerRepositoryImpl @Inject constructor(
    private val pagingNotifyProvider: javax.inject.Provider<PagingNotify>,
) : PagerRepository {
    override fun noticePageStream(): Flow<PagingData<Notice>> =
        Pager(PagingConfig(pageSize = 30)) { pagingNotifyProvider.get() }.flow
}