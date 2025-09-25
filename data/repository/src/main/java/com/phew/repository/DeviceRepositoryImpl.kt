package com.phew.repository

import com.phew.device.dataStore.DataStore
import com.phew.device.device.Device
import com.phew.device.dto.UserInfoDTO
import com.phew.domain.dto.Token
import com.phew.domain.dto.UserInfo
import com.phew.domain.repository.DeviceRepository
import javax.inject.Inject


class DeviceRepositoryImpl @Inject constructor(
    private val device: Device,
    private val dataSource: DataStore,
) : DeviceRepository {
    override suspend fun requestDeviceId(): String {
        return device.deviceId()
    }

    override suspend fun requestToken(key: String): Pair<String, String> {
        return dataSource.getToken(key)
    }

    override suspend fun saveToken(key: String, data: Token): Boolean {
        return dataSource.insertToken(key = key, data = Pair(data.refreshToken, data.refreshToken))
    }

    override suspend fun firebaseToken(): String {
        return device.firebaseToken()
    }

    override suspend fun requestGetSaveFirebaseToken(key: String): String {
        return dataSource.getFirebaseToken(key)
    }

    override suspend fun requestSaveFirebaseToken(key: String, data: String): Boolean {
        return dataSource.insertFirebaseToken(key = key, data = data)
    }

    override suspend fun requestSaveNotify(key: String, data: Boolean): Boolean {
        return dataSource.insertNotifyAgree(key = key, data = data)
    }

    override suspend fun requestGetNotify(key: String): Boolean {
        return dataSource.getNotifyAgree(key)
    }

    override suspend fun saveUserInfo(
        key: String,
        nickName: String,
        isNotifyAgree: Boolean,
        agreedToTermsOfService: Boolean,
        agreedToLocationTerms: Boolean,
        agreedToPrivacyPolicy: Boolean,
    ): Boolean {
        return dataSource.saveUserInfo(
            key = key,
            data = UserInfoDTO(
                nickName = nickName,
                isNotifyAgree = isNotifyAgree,
                agreedToLocationTerms = agreedToLocationTerms,
                agreedToTermsOfService = agreedToTermsOfService,
                agreedToPrivacyPolicy = agreedToPrivacyPolicy
            )
        )
    }

    override suspend fun getUserInfo(key: String): UserInfo? {
        val request = dataSource.getUserInfo(key = key) ?: return null
        return UserInfo(
            nickName = request.nickName,
            isNotifyAgree = request.isNotifyAgree,
            agreedToLocationTerms = request.agreedToLocationTerms,
            agreedToPrivacyPolicy = request.agreedToPrivacyPolicy,
            agreedToTermsOfService = request.agreedToTermsOfService
        )
    }
}