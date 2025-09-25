package com.phew.device.dataStore

import com.phew.device.dto.UserInfoDTO


interface DataStore {
    suspend fun insertToken(key: String, data: Pair<String, String>): Boolean
    suspend fun getToken(key: String): Pair<String, String>
    suspend fun remove(key: String): Boolean
    suspend fun insertFirebaseToken(key: String, data: String): Boolean
    suspend fun getFirebaseToken(key: String): String
    suspend fun insertNotifyAgree(key: String, data: Boolean): Boolean
    suspend fun getNotifyAgree(key: String): Boolean
    suspend fun saveUserInfo(key: String, data: UserInfoDTO): Boolean
    suspend fun getUserInfo(key: String): UserInfoDTO?
    suspend fun setLocationPermissionIsAsk(key: String, data: Boolean): Boolean
    suspend fun getLocationPermissionIsAsk(key: String): Boolean
}