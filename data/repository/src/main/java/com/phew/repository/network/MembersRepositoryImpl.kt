package com.phew.repository.network

import com.phew.core_common.DataResult
import com.phew.core_common.log.SooumLog
import com.phew.datastore_local.DataStore
import com.phew.device_info.DeviceInfo
import com.phew.domain.model.RejoinableDate
import com.phew.domain.model.TransferCode
import com.phew.domain.repository.network.MembersRepository
import com.phew.network.dto.request.account.TransferAccountRequestDTO
import com.phew.network.dto.request.account.WithdrawalRequestDTO
import com.phew.network.retrofit.MembersHttp
import com.phew.repository.mapper.apiCall
import com.phew.repository.mapper.toDomain
import javax.inject.Inject

class MembersRepositoryImpl @Inject constructor(
    private val membersHttp: MembersHttp,
    private val deviceInfo: DeviceInfo,
    private val dataStore: DataStore
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
    
    override suspend fun withdrawalAccount(reason: String): Result<Unit> {
        SooumLog.d(TAG, "withdrawalAccount - reason: $reason")
        
        return try {
            // DataStore에서 토큰 가져오기
            val tokenData = dataStore.getToken("user_token")
            
            val request = WithdrawalRequestDTO(
                accessToken = tokenData.accessToken,
                refreshToken = tokenData.refreshToken,
                reason = reason
            )
            
            when (val result = apiCall(
                apiCall = { membersHttp.withdrawalAccount(request) },
                mapper = { Unit }
            )) {
                is DataResult.Success -> {
                    val clearResult = dataStore.clearAllData()
                    if (clearResult) {
                        SooumLog.d(TAG, "Successfully cleared all data after withdrawal")
                        Result.success(Unit)
                    } else {
                        SooumLog.w(TAG, "Failed to clear data after withdrawal, but account was withdrawn")
                        Result.success(Unit) // 탈퇴는 성공했으므로 여전히 성공으로 처리
                    }
                }
                is DataResult.Fail -> Result.failure(
                    result.throwable ?: Exception("Failed to withdrawal account: ${result.message}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getRejoinableDate(): Result<RejoinableDate> {
        SooumLog.d(TAG, "getRejoinableDate")
        
        return when (val result = apiCall(
            apiCall = { membersHttp.getRejoinableDate() },
            mapper = { it.toDomain() }
        )) {
            is DataResult.Success -> Result.success(result.data)
            is DataResult.Fail -> Result.failure(
                result.throwable ?: Exception("Failed to get rejoinable date: ${result.message}")
            )
        }
    }
}

private const val TAG = "MembersRepository"