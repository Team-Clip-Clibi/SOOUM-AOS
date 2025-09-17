package com.phew.repository

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.DataResult
import com.phew.domain.repository.NetworkRepository
import com.phew.network.Http
import javax.inject.Inject

class NetworkRepositoryImpl @Inject constructor(private val http: Http) : NetworkRepository {
    override suspend fun requestAppVersion(type: String, appVersion: String): DataResult<String> {
        try {
            val result = http.getVersion(
                type = type,
                data = appVersion
            )
            if (!result.isSuccessful) {
                return DataResult.Fail(code = result.code(), message = result.message())
            }
            if (result.body() == null) {
                return DataResult.Fail(code = result.code(), message = result.message())
            }
            return DataResult.Success(result.body()!!.status)
        } catch (e: Exception) {
            e.printStackTrace()
            return DataResult.Fail(
                code = APP_ERROR_CODE,
                message = e.message,
                throwable = e
            )
        }
    }
}