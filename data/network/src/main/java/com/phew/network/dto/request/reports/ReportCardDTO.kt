package com.phew.network.dto.request.reports

import kotlinx.serialization.Serializable

@Serializable
data class ReportCardDTO(
    val reportType: String,
)