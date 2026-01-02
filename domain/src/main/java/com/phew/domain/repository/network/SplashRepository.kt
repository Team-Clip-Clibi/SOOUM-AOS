package com.phew.domain.repository.network

import com.phew.core_common.DataResult
import com.phew.domain.model.AppVersionStatus

interface SplashRepository {
    suspend fun requestAppVersion(type: String, appVersion: String): DataResult<AppVersionStatus>
    suspend fun requestUpdateFcm(fcmToken: String): DataResult<Unit>
}