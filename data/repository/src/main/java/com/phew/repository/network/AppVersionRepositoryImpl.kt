package com.phew.repository.network

import com.phew.core_common.DataResult
import com.phew.domain.model.AppVersionStatus
import com.phew.domain.repository.network.AppVersionRepository
import com.phew.network.retrofit.AppVersionHttp
import com.phew.repository.mapper.apiCall
import com.phew.repository.mapper.toDomain
import javax.inject.Inject

class AppVersionRepositoryImpl @Inject constructor(
    private val appVersionHttp: AppVersionHttp
) : AppVersionRepository {
    
    override suspend fun checkAppVersion(type: String, version: String): Result<AppVersionStatus> {
        return when (val result = apiCall(
            apiCall = { appVersionHttp.checkAppVersion(type, version) },
            mapper = { it.toDomain() }
        )) {
            is DataResult.Success -> Result.success(result.data)
            is DataResult.Fail -> Result.failure(
                result.throwable ?: Exception("Failed to check app version: ${result.message}")
            )
        }
    }
}