package com.phew.domain.repository.network

import com.phew.core_common.DataResult

interface ReportsRepository {
    suspend fun requestReportCards(reason: String, cardId: Long): DataResult<Unit>
}