package com.phew.domain.usecase

import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.exception.asSooumException
import com.phew.core_common.resultFailure
import com.phew.domain.dto.Notice
import com.phew.domain.dto.NoticeSource
import com.phew.domain.repository.network.NotifyRepository
import javax.inject.Inject

class GetFeedNotification @Inject constructor(private val repository: NotifyRepository) {
    suspend operator fun invoke(source: NoticeSource = NoticeSource.NOTIFICATION): Result<List<Notice>> {
        return repository.requestNotice(pageSize = 3, source = source).fold(
            onSuccess = { request ->
                Result.success(request.second.sortedByDescending { data -> data.id }.take(3))
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
    }
}
