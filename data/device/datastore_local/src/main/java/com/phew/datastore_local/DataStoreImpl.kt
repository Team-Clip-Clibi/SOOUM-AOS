package com.phew.datastore_local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.phew.core_common.ERROR
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_NO_DATA
import com.phew.datastore_local.dto.TokenDTO
import com.phew.datastore_local.dto.UserInfoDTO
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DataStoreImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileName: String,
) : DataStore {
    private val gson = Gson()
    private val sharedPreferences: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        try {
            createEncryptedSharedPreferences(masterKey)
        } catch (e: Exception) {
            e.printStackTrace()
            context.deleteSharedPreferences(fileName)
            createEncryptedSharedPreferences(masterKey)
        }
    }

    private fun createEncryptedSharedPreferences(masterKey: MasterKey): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            fileName,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Volatile
    private var cachedToken: TokenDTO? = null

    @Volatile
    private var userInfo: UserInfoDTO? = null
    override suspend fun insertToken(key: String, data: Pair<String, String>): Boolean {
        try {
            val token = TokenDTO(data.first, data.second)
            val jsonString = gson.toJson(token)
            sharedPreferences.edit(commit = true) { putString(key, jsonString) }
            cachedToken = token
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override suspend fun getToken(key: String): TokenDTO {
        try {
            cachedToken?.let { token ->
                return TokenDTO(token.refreshToken, token.accessToken)
            }
            val jsonString = sharedPreferences.getString(key, ERROR_NO_DATA)
            if (jsonString == ERROR_NO_DATA) {
                return TokenDTO(ERROR_NO_DATA, ERROR_NO_DATA)
            }
            val token = gson.fromJson(jsonString, TokenDTO::class.java)
            cachedToken = token
            return TokenDTO(token.refreshToken, token.accessToken)
        } catch (e: Exception) {
            e.printStackTrace()
            return TokenDTO(ERROR_FAIL_JOB, ERROR_FAIL_JOB)
        }
    }

    override suspend fun remove(key: String): Boolean {
        try {
            sharedPreferences.edit { remove(key) }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override suspend fun insertFirebaseToken(key: String, data: String): Boolean {
        try {
            sharedPreferences.edit(commit = true) { putString(key, data) }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override suspend fun getFirebaseToken(key: String): String {
        try {
            val token = sharedPreferences.getString(key, "") ?: return ERROR_NO_DATA
            return token
        } catch (e: Exception) {
            e.printStackTrace()
            return ERROR
        }
    }

    override suspend fun insertNotifyAgree(key: String, data: Boolean): Boolean {
        try {
            sharedPreferences.edit(commit = true) { putBoolean(key, data) }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override suspend fun getNotifyAgree(key: String): Boolean {
        try {
            val isAgree = sharedPreferences.getBoolean(key, false)
            return isAgree
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override suspend fun saveUserInfo(key: String, data: UserInfoDTO): Boolean {
        try {
            val jsonString = gson.toJson(data)
            sharedPreferences.edit(commit = true) { putString(key, jsonString) }
            userInfo = data
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override suspend fun getUserInfo(key: String): UserInfoDTO? {
        try {
            userInfo?.let { data ->
                return data
            }
            val jsonString = sharedPreferences.getString(key, null) ?: return null
            val userInfoDTO = gson.fromJson(jsonString, UserInfoDTO::class.java)
            this.userInfo = userInfoDTO
            return userInfoDTO
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override suspend fun clearAllData(): Boolean {
        try {
            sharedPreferences.edit(commit = true) { clear() }
            cachedToken = null
            userInfo = null
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}