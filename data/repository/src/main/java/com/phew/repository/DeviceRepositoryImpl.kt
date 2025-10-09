package com.phew.repository


import com.phew.device_info.DeviceInfo
import com.phew.location_provider.LocationProvider
import com.phew.datastore_local.DataStore
import com.phew.datastore_local.dto.UserInfoDTO
import com.phew.domain.dto.Location
import com.phew.domain.dto.Token
import com.phew.domain.dto.UserInfo
import com.phew.domain.repository.DeviceRepository
import javax.inject.Inject


class DeviceRepositoryImpl @Inject constructor(
    private val dataStoreLocal: DataStore,
    private val deviceInfo: DeviceInfo,
    private val location: LocationProvider,
) : DeviceRepository {
    override suspend fun requestDeviceId(): String {
        return deviceInfo.deviceId()
    }

    override suspend fun requestToken(key: String): Pair<String, String> {
        val data = dataStoreLocal.getToken(key)
        return Pair(data.refreshToken, data.accessToken)
    }

    override suspend fun saveToken(key: String, data: Token): Boolean {
        return dataStoreLocal.insertToken(
            key = key,
            data = Pair(data.refreshToken, data.refreshToken)
        )
    }

    override suspend fun firebaseToken(): String {
        return deviceInfo.firebaseToken()
    }

    override suspend fun requestGetSaveFirebaseToken(key: String): String {
        return dataStoreLocal.getFirebaseToken(key)
    }

    override suspend fun requestSaveFirebaseToken(key: String, data: String): Boolean {
        return dataStoreLocal.insertFirebaseToken(key = key, data = data)
    }

    override suspend fun requestSaveNotify(key: String, data: Boolean): Boolean {
        return dataStoreLocal.insertNotifyAgree(key = key, data = data)
    }

    override suspend fun requestGetNotify(key: String): Boolean {
        return dataStoreLocal.getNotifyAgree(key)
    }

    override suspend fun saveUserInfo(
        key: String,
        nickName: String,
        isNotifyAgree: Boolean,
        agreedToTermsOfService: Boolean,
        agreedToLocationTerms: Boolean,
        agreedToPrivacyPolicy: Boolean,
    ): Boolean {
        return dataStoreLocal.saveUserInfo(
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
        val request = dataStoreLocal.getUserInfo(key = key) ?: return null
        return UserInfo(
            nickName = request.nickName,
            isNotifyAgree = request.isNotifyAgree,
            agreedToLocationTerms = request.agreedToLocationTerms,
            agreedToPrivacyPolicy = request.agreedToPrivacyPolicy,
            agreedToTermsOfService = request.agreedToTermsOfService
        )
    }

    override suspend fun requestLocation(): Location {
        return Location(
            latitude = location.location().latitude,
            longitude = location.location().longitude
        )
    }

    override suspend fun deleteDataStoreInfo(key: String): Boolean {
        return dataStoreLocal.remove(key)
    }

    override suspend fun getLocationPermission(): Boolean {
        return location.locationPermissionCheck()
    }
}