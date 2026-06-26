package com.phew.repository.network

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
        return apiCall(
            apiCall = { appVersionHttp.checkAppVersion(type, version) },
            mapper = { it.toDomain() }
        )
    }
}
