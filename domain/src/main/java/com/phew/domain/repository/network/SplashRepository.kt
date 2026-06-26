package com.phew.domain.repository.network

import com.phew.domain.model.AppVersionStatus

interface SplashRepository {
    suspend fun requestAppVersion(type: String, appVersion: String): Result<AppVersionStatus>
    suspend fun requestUpdateFcm(fcmToken: String): Result<Unit>
}