package com.phew.network.retrofit

import com.phew.network.BuildConfig
import com.phew.network.dto.request.reports.ReportCardDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ReportHttp {
    /**
     * Report card
     */
    @POST(BuildConfig.API_URL_REPORTS_CARDS)
    suspend fun requestReportCard(
        @Path("cardId ") cardId: Int,
        @Body request: ReportCardDTO
    ): Response<Unit>

}