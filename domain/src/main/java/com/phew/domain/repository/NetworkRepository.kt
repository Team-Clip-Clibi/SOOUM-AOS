package com.phew.domain.repository

import com.phew.core_common.DataResult

interface NetworkRepository {
    suspend fun requestAppVersion(type : String,appVersion : String) : DataResult<String>
}