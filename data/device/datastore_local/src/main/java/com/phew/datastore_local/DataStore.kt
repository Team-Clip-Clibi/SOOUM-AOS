package com.phew.datastore_local

import com.phew.datastore_local.dto.ProfileInfoDTO
import com.phew.datastore_local.dto.TokenDTO
import com.phew.datastore_local.dto.UserInfoDTO

interface DataStore {
    suspend fun insertToken(key: String, data: Pair<String, String>): Boolean
    suspend fun getToken(key: String): TokenDTO
    suspend fun remove(key: String): Boolean
    suspend fun insertFirebaseToken(key: String, data: String): Boolean
    suspend fun getFirebaseToken(key: String): String
    suspend fun insertNotifyAgree(key: String, data: Boolean): Boolean
    suspend fun getNotifyAgree(key: String): Boolean
    suspend fun saveUserInfo(key: String, data: UserInfoDTO): Boolean
    suspend fun getUserInfo(key: String): UserInfoDTO?
    suspend fun clearAllData(): Boolean
    suspend fun saveNickName(
        profileKey : String,
        data : ProfileInfoDTO
    ): Boolean
    suspend fun getNickName(
        profileKey : String
    ) : ProfileInfoDTO?
}