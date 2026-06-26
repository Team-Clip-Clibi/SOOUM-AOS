package com.phew.repository.network

import com.phew.core_common.mapSooumFailure
import com.phew.core_common.log.SooumLog
import com.phew.datastore_local.DataStore
import com.phew.device_info.DeviceInfo
import com.phew.domain.dto.Alarm
import com.phew.domain.model.RejoinableDate
import com.phew.domain.model.TransferCode
import com.phew.domain.repository.network.MembersRepository
import com.phew.network.dto.request.account.TransferAccountRequestDTO
import com.phew.network.dto.request.account.WithdrawalRequestDTO
import com.phew.network.retrofit.MembersHttp
import com.phew.repository.mapper.apiCall
import com.phew.repository.mapper.toDTO
import com.phew.repository.mapper.toDomain
import javax.inject.Inject

class MembersRepositoryImpl @Inject constructor(
    private val membersHttp: MembersHttp,
    private val deviceInfo: DeviceInfo,
    private val dataStore: DataStore
) : MembersRepository {

    override suspend fun getActivityRestrictionDate(): Result<String?> {
        SooumLog.d(TAG, "getActivityRestrictionDate")
        return apiCall(
            apiCall = { membersHttp.getActivityRestrictionDate() },
            mapper = { it.activityRestrictionDate }
        )
    }

    override suspend fun getTransferCode(): Result<TransferCode> {
        return apiCall(
            apiCall = { membersHttp.getTransferCode() },
            mapper = { it.toDomain() }
        )
    }

    override suspend fun refreshTransferCode(): Result<TransferCode> {
        return apiCall(
            apiCall = { membersHttp.refreshTransferCode() },
            mapper = { it.toDomain() }
        )
    }

    override suspend fun transferAccount(transferCode: String, deviceId: String): Result<Unit> {
        SooumLog.d(TAG, "transferAccount - transferCode: $transferCode")

        return runCatching {
            val deviceModel = deviceInfo.modelName()
            val deviceOsVersion = deviceInfo.osVersion()
            val request = TransferAccountRequestDTO(
                transferCode = transferCode,
                encryptedDeviceId = deviceId,
                deviceType = "ANDROID",
                deviceModel = deviceModel,
                deviceOsVersion = deviceOsVersion
            )
            apiCall(
                apiCall = { membersHttp.transferAccount(request) },
                mapper = { Unit }
            )
        }.mapSooumFailure().fold(
            onSuccess = { it },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun withdrawalAccount(reason: String): Result<Unit> {
        SooumLog.d(TAG, "withdrawalAccount - reason: $reason")

        return runCatching {
            val tokenData = dataStore.getToken("user_token")

            val request = WithdrawalRequestDTO(
                accessToken = tokenData.accessToken,
                refreshToken = tokenData.refreshToken,
                reason = reason
            )

            apiCall(
                apiCall = { membersHttp.withdrawalAccount(request) },
                mapper = { Unit }
            ).fold(
                onSuccess = {
                    val clearResult = dataStore.clearAllData()
                    if (clearResult) {
                        SooumLog.d(TAG, "Successfully cleared all data after withdrawal")
                        Result.success(Unit)
                    } else {
                        SooumLog.w(
                            TAG,
                            "Failed to clear data after withdrawal, but account was withdrawn"
                        )
                        Result.success(Unit) // 탈퇴는 성공했으므로 여전히 성공으로 처리
                    }
                },
                onFailure = { Result.failure(it) },
            )
        }.mapSooumFailure().fold(
            onSuccess = { it },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun getRejoinableDate(): Result<RejoinableDate> {
        SooumLog.d(TAG, "getRejoinableDate")

        return apiCall(
            apiCall = { membersHttp.getRejoinableDate() },
            mapper = { it.toDomain() }
        )
    }

    override suspend fun toggleNotification(isAllowNotify: Alarm): Result<Unit> {
        SooumLog.d(TAG, "toggleNotification - isAllowNotify: $isAllowNotify")
        return (apiCall(
            apiCall = { membersHttp.toggleNotification(isAllowNotify.toDTO()) },
            mapper = { _ ->  }
        ))
    }

    override suspend fun getToggleNotification(): Result<Alarm> {
        return apiCall(
            apiCall = { membersHttp.getToggleNotification() },
            mapper = { result -> result.toDomain() }
        )
    }
}

private const val TAG = "MembersRepository"
