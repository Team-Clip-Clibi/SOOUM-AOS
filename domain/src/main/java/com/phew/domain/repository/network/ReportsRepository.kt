package com.phew.domain.repository.network


interface ReportsRepository {
    suspend fun requestReportCards(reason: String, cardId: Long): Result<Unit>
}