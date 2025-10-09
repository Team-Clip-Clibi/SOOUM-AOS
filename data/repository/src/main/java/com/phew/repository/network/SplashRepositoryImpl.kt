package com.phew.repository.network

import com.phew.core_common.DataResult
import com.phew.domain.repository.network.SplashRepository
import com.phew.network.dto.FCMToken
import com.phew.network.retrofit.SplashHttp
import com.phew.repository.mapper.apiCall
import javax.inject.Inject

class SplashRepositoryImpl @Inject constructor(private val splashHttp: SplashHttp) :
    SplashRepository {
    override suspend fun requestAppVersion(
        type: String,
        appVersion: String
    ): DataResult<String> {
        return apiCall(
            apiCall = { splashHttp.getVersion(type = type, data = appVersion) },
            mapper = { result -> result.status }
        )
    }

    override suspend fun requestUpdateFcm(fcmToken: String): DataResult<Unit> {
        return apiCall(
            apiCall = { splashHttp.requestUpdateFcm(body = FCMToken(fcmToken)) },
            mapper = { result -> result }
        )
    }

}