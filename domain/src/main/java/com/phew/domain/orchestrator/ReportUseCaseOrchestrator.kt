package com.phew.domain.orchestrator

import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.exception.asSooumException
import com.phew.core_common.resultFailure
import com.phew.domain.dto.ReportReason
import com.phew.domain.repository.network.ReportsRepository
import javax.inject.Inject

/**
 * 신고 화면에서 카드 신고 요청과 신고 실패 메시지 매핑을 조율합니다.
 */
class ReportUseCaseOrchestrator @Inject constructor(
    private val reportsRepository: ReportsRepository,
) {
    suspend fun reportCard(cardId: String, reason: ReportReason): Result<Unit> {
        if (cardId.isEmpty()) {
            return resultFailure(ERROR_FAIL_JOB)
        }
        return reportsRepository.requestReportCards(
            cardId = cardId.toLong(),
            reason = reason.name,
        ).fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { throwable ->
                val exception = throwable.asSooumException()
                when (exception.code) {
                    HTTP_INVALID_TOKEN -> resultFailure(ERROR_LOGOUT)
                    else -> resultFailure(exception.message.ifBlank { ERROR_NETWORK })
                }
            },
        )
    }
}
