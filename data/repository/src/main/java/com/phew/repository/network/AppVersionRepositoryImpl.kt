package com.phew.repository.network

import com.phew.domain.model.AppVersionStatus
import com.phew.domain.repository.network.AppVersionRepository
import com.phew.network.retrofit.AppVersionHttp
import com.phew.repository.mapper.toDomain
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppVersionRepositoryImpl @Inject constructor(
    private val appVersionHttp: AppVersionHttp
) : AppVersionRepository {
    
    override suspend fun checkAppVersion(type: String, version: String): Result<AppVersionStatus> {
        return try {
            val response = appVersionHttp.checkAppVersion(type, version)
            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Result.success(dto.toDomain())
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("Failed to check app version: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}