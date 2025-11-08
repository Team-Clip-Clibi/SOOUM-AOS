package com.phew.repository.network

import com.phew.core_common.log.SooumLog
import com.phew.domain.model.TransferCode
import com.phew.domain.repository.network.MembersRepository
import com.phew.network.retrofit.MembersHttp
import com.phew.repository.mapper.toDomain
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MembersRepositoryImpl @Inject constructor(
    private val membersHttp: MembersHttp
) : MembersRepository {
    
    override suspend fun getActivityRestrictionDate(): Result<String?> {
        return try {
            SooumLog.d(TAG, "getActivityRestrictionDate")
            val response = membersHttp.getActivityRestrictionDate()
            if (response.isSuccessful) {
                Result.success(response.body()?.activityRestrictionDate)
            } else {
                Result.failure(Exception("Failed to get activity restriction date: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getTransferCode(): Result<TransferCode> {
        return try {
            val response = membersHttp.getTransferCode()
            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Result.success(dto.toDomain())
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("Failed to get transfer code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun refreshTransferCode(): Result<TransferCode> {
        return try {
            val response = membersHttp.refreshTransferCode()
            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Result.success(dto.toDomain())
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("Failed to refresh transfer code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private const val TAG = "MembersRepository"