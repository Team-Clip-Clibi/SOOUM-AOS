package com.phew.domain.usecase

import androidx.paging.PagingData
import com.phew.domain.dto.Notice
import com.phew.domain.dto.NoticeSource
import com.phew.domain.repository.PagerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotification @Inject constructor(private val repository: PagerRepository) {
    operator fun invoke(source: NoticeSource = NoticeSource.SETTINGS): Flow<PagingData<Notice>> = repository.noticePageStream(source)
}