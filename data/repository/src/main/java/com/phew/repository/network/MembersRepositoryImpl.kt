package com.phew.repository.network

import com.phew.core_common.DataResult
import com.phew.core_common.log.SooumLog
import com.phew.device_info.DeviceInfo
import com.phew.domain.model.TransferCode
import com.phew.domain.repository.network.MembersRepository
import com.phew.network.dto.request.account.TransferAccountRequestDTO
import com.phew.network.retrofit.MembersHttp
import com.phew.repository.mapper.apiCall
import com.phew.repository.mapper.toDomain
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MembersRepositoryImpl @Inject constructor(
    private val membersHttp: MembersHttp,
    private val deviceInfo: DeviceInfo
) : MembersRepository {
    
    override suspend fun getActivityRestrictionDate(): Result<String?> {
        SooumLog.d(TAG, "getActivityRestrictionDate")
        return when (val result = apiCall(
            apiCall = { membersHttp.getActivityRestrictionDate() },
            mapper = { it.activityRestrictionDate }
        )) {
            is DataResult.Success -> Result.success(result.data)
            is DataResult.Fail -> Result.failure(
                result.throwable ?: Exception("Failed to get activity restriction date: ${result.message}")
            )
        }
    }
    
    override suspend fun getTransferCode(): Result<TransferCode> {
        return when (val result = apiCall(
            apiCall = { membersHttp.getTransferCode() },
            mapper = { it.toDomain() }
        )) {
            is DataResult.Success -> Result.success(result.data)
            is DataResult.Fail -> Result.failure(
                result.throwable ?: Exception("Failed to get transfer code: ${result.message}")
            )
        }
    }
    
    override suspend fun refreshTransferCode(): Result<TransferCode> {
        return when (val result = apiCall(
            apiCall = { membersHttp.refreshTransferCode() },
            mapper = { it.toDomain() }
        )) {
            is DataResult.Success -> Result.success(result.data)
            is DataResult.Fail -> Result.failure(
                result.throwable ?: Exception("Failed to refresh transfer code: ${result.message}")
            )
        }
    }
    
    override suspend fun transferAccount(transferCode: String): Result<Unit> {
        SooumLog.d(TAG, "transferAccount - transferCode: $transferCode")
        
        return try {
            val deviceId = deviceInfo.deviceId()
            val deviceModel = deviceInfo.modelName()
            val deviceOsVersion = deviceInfo.osVersion()
            
            val request = TransferAccountRequestDTO(
                transferCode = transferCode,
                encryptedDeviceId = deviceId, // 실제로는 암호화된 값이어야 함
                deviceType = "ANDROID",
                deviceModel = deviceModel,
                deviceOsVersion = deviceOsVersion
            )
            
            when (val result = apiCall(
                apiCall = { membersHttp.transferAccount(request) },
                mapper = { Unit }
            )) {
                is DataResult.Success -> Result.success(Unit)
                is DataResult.Fail -> Result.failure(
                    result.throwable ?: Exception("Failed to transfer account: ${result.message}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private const val TAG = "MembersRepository"