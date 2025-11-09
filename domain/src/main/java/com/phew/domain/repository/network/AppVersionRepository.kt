package com.phew.domain.repository.network

import com.phew.domain.model.AppVersionStatus

interface AppVersionRepository {
    suspend fun checkAppVersion(type: String, version: String): Result<AppVersionStatus>
}