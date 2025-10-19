package com.phew.domain.repository.network

import com.phew.core_common.DataResult

interface SplashRepository {
    suspend fun requestAppVersion(type: String, appVersion: String): DataResult<String>
    suspend fun requestUpdateFcm(fcmToken: String): DataResult<Unit>
}