package com.phew.domain.repository

import com.phew.domain.dto.UserInfo

interface DeviceRepository {
    suspend fun requestDeviceId(): String
    suspend fun requestToken(key: String): Pair<String, String>
    suspend fun saveToken(key: String, data: Pair<String, String>): Boolean
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
}