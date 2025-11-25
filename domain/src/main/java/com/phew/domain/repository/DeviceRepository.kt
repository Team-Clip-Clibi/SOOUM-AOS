package com.phew.domain.repository

import com.phew.domain.dto.Location
import com.phew.domain.dto.Token
import com.phew.domain.dto.UserInfo

interface DeviceRepository {
    suspend fun requestDeviceId(): String
    suspend fun requestDeviceModel(): String
    suspend fun requestDeviceOS(): String
    suspend fun requestToken(key: String): Pair<String, String>
    suspend fun saveToken(key: String, data: Token): Boolean
    suspend fun firebaseToken(): String
    suspend fun requestGetSaveFirebaseToken(key: String): String
    suspend fun requestSaveFirebaseToken(key: String, data: String): Boolean
    suspend fun requestSaveNotify(key: String, data: Boolean): Boolean
    suspend fun requestGetNotify(key: String): Boolean
    suspend fun saveUserInfo(
        key: String,
        nickName: String,
        isNotifyAgree: Boolean,
        agreedToTermsOfService: Boolean,
        agreedToLocationTerms: Boolean,
        agreedToPrivacyPolicy: Boolean,
    ): Boolean

    suspend fun getUserInfo(key: String): UserInfo?
    suspend fun requestLocation(): Location
    suspend fun deleteDataStoreInfo(key: String): Boolean
    suspend fun getLocationPermission(): Boolean
    suspend fun getAppVersion(): String
    suspend fun deleteAll(): Boolean
    suspend fun saveProfileInfo(
        profileKey: String,
        nickName: String,
        profileImageUrl: String,
        profileImageName: String,
    ): Boolean

    suspend fun getProfileInfo(
        profileKey: String,
    ): Triple<String, String, String>?
}