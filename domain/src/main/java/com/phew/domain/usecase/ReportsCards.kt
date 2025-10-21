package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.domain.dto.ReportReason
import com.phew.domain.repository.network.ReportsRepository
import javax.inject.Inject

class ReportsCards @Inject constructor(private val repository: ReportsRepository) {
    data class Param(
        val cardId: String,
        val reason: ReportReason,
    )

    suspend operator fun invoke(param: Param): DomainResult<Unit, String> {
        val request = repository.requestReportCards(
            cardId = param.cardId.toInt(),
            reason = param.reason.name
        )
        return when (request) {
            is DataResult.Fail -> {
                when (request.code) {
                    HTTP_INVALID_TOKEN -> {
                        DomainResult.Failure(ERROR_LOGOUT)
                    }

                    else -> DomainResult.Failure(request.message ?: ERROR_NETWORK)
                }
            }

            is DataResult.Success -> {
                DomainResult.Success(Unit)
            }
        }
    }
}