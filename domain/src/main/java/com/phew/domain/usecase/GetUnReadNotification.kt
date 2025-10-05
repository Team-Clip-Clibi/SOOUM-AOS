package com.phew.domain.usecase

import androidx.paging.PagingData
import com.phew.domain.dto.Notification
import com.phew.domain.repository.PagerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUnReadNotification @Inject constructor(private val repository: PagerRepository) {
    operator fun invoke() : Flow<PagingData<Notification>> = repository.notificationUnRead()
}