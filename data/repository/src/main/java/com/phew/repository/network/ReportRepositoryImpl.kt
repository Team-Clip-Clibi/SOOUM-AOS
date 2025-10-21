package com.phew.repository.network

import com.phew.core_common.DataResult
import com.phew.domain.repository.network.ReportsRepository
import com.phew.network.dto.request.reports.ReportCardDTO
import com.phew.network.retrofit.ReportHttp
import com.phew.repository.mapper.apiCall
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(private val http: ReportHttp) : ReportsRepository {
    override suspend fun requestReportCards(reason: String, cardId: Int): DataResult<Unit> {
        return apiCall(
            apiCall = { http.requestReportCard(cardId = cardId, request = ReportCardDTO(reason)) },
            mapper = { _ -> Unit }
        )
    }
}