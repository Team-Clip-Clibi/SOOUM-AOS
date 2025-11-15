package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.domain.dto.Notice
import com.phew.domain.dto.NoticeSource
import com.phew.domain.repository.network.NotifyRepository
import javax.inject.Inject

class GetFeedNotification @Inject constructor(private val repository: NotifyRepository) {
    suspend operator fun invoke(source: NoticeSource = NoticeSource.NOTIFICATION): DomainResult<List<Notice>, String> {
        when (val request = repository.requestNotice(pageSize = 3, source = source)) {
            is DataResult.Fail -> {
                if (request.code == HTTP_INVALID_TOKEN) {
                    return DomainResult.Failure(ERROR_LOGOUT)
                }
                return DomainResult.Failure(ERROR_NETWORK)
            }

            is DataResult.Success -> {
                val data = request.data.second.sortedByDescending { data -> data.id }.take(3)
                return DomainResult.Success(data)
            }
        }
    }
}