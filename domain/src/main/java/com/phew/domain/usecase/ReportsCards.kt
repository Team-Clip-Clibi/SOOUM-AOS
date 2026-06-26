package com.phew.domain.usecase

import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.exception.asSooumException
import com.phew.core_common.resultFailure
import com.phew.domain.dto.ReportReason
import com.phew.domain.repository.network.ReportsRepository
import javax.inject.Inject

class ReportsCards @Inject constructor(private val repository: ReportsRepository) {
    data class Param(
        val cardId: String,
        val reason: ReportReason,
    )

    suspend operator fun invoke(param: Param): Result<Unit> {
        if (param.cardId.isEmpty()) {
            return com.phew.core_common.resultFailure(ERROR_FAIL_JOB)
        }
        val request = repository.requestReportCards(
            cardId = param.cardId.toLong(),
            reason = param.reason.name
        )
        return request.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { throwable ->
                val exception = throwable.asSooumException()
                when (exception.code) {
                    HTTP_INVALID_TOKEN -> resultFailure(ERROR_LOGOUT)
                    else -> resultFailure(exception.message.ifBlank { ERROR_NETWORK })
                }
            }
        )
    }
}
