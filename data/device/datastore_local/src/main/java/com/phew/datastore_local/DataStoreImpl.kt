package com.phew.datastore_local

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.phew.core_common.ERROR
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_NO_DATA
import com.phew.datastore_local.dto.ProfileInfoDTO
import com.phew.datastore_local.dto.TokenDTO
import com.phew.datastore_local.dto.UserInfoDTO
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataStoreImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileName: String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
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

    @Volatile
    private var profileInfoDTO: ProfileInfoDTO? = null

    @SuppressLint("UseKtx")
    private fun SharedPreferences.commit(block: SharedPreferences.Editor.() -> Unit): Boolean {
        val editor = edit()
        block(editor)
        return editor.commit()
    }

    override suspend fun insertToken(key: String, data: Pair<String, String>): Boolean = withContext(ioDispatcher) {
        runCatching {
            val token = TokenDTO(data.first, data.second)
            val jsonString = gson.toJson(token)
            val success = sharedPreferences.commit { putString(key, jsonString) }
            if (success) {
                cachedToken = token
            }
            success
        }.getOrElse {
            it.printStackTrace()
            false
        }
    }

    override suspend fun getToken(key: String): TokenDTO = withContext(ioDispatcher) {
        runCatching {
            cachedToken?.let { token ->
                return@withContext TokenDTO(token.refreshToken, token.accessToken)
            }
            val jsonString = sharedPreferences.getString(key, ERROR_NO_DATA)
            if (jsonString == ERROR_NO_DATA) {
                return@withContext TokenDTO(ERROR_NO_DATA, ERROR_NO_DATA)
            }
            val token = gson.fromJson(jsonString, TokenDTO::class.java)
            cachedToken = token
            TokenDTO(token.refreshToken, token.accessToken)
        }.getOrElse {
            it.printStackTrace()
            TokenDTO(ERROR_FAIL_JOB, ERROR_FAIL_JOB)
        }
    }

    override suspend fun remove(key: String): Boolean = withContext(ioDispatcher) {
        runCatching {
            val success = sharedPreferences.commit { remove(key) }
            if (success) {
                cachedToken = null
                userInfo = null
                profileInfoDTO = null
            }
            success
        }.getOrElse {
            it.printStackTrace()
            false
        }
    }

    override suspend fun insertFirebaseToken(key: String, data: String): Boolean = withContext(ioDispatcher) {
        runCatching {
            sharedPreferences.commit { putString(key, data) }
        }.getOrElse {
            it.printStackTrace()
            false
        }
    }

    override suspend fun getFirebaseToken(key: String): String = withContext(ioDispatcher) {
        runCatching {
            sharedPreferences.getString(key, "") ?: ERROR_NO_DATA
        }.getOrElse {
            it.printStackTrace()
            ERROR
        }
    }

    override suspend fun insertNotifyAgree(key: String, data: Boolean): Boolean = withContext(ioDispatcher) {
        runCatching {
            sharedPreferences.commit { putBoolean(key, data) }
        }.getOrElse {
            it.printStackTrace()
            false
        }
    }

    override suspend fun getNotifyAgree(key: String): Boolean = withContext(ioDispatcher) {
        runCatching {
            sharedPreferences.getBoolean(key, false)
        }.getOrElse {
            it.printStackTrace()
            false
        }
    }

    override suspend fun saveUserInfo(key: String, data: UserInfoDTO): Boolean = withContext(ioDispatcher) {
        runCatching {
            val jsonString = gson.toJson(data)
            val success = sharedPreferences.commit { putString(key, jsonString) }
            if (success) {
                userInfo = data
            }
            success
        }.getOrElse {
            it.printStackTrace()
            false
        }
    }

    override suspend fun saveNickName(
        profileKey: String,
        data: ProfileInfoDTO,
    ): Boolean = withContext(ioDispatcher) {
        runCatching {
            if (profileInfoDTO != null && profileInfoDTO == data) {
                return@withContext true
            }
            val success = when (val beforeData = sharedPreferences.getString(profileKey, null)) {
                null -> {
                    val jsonString = gson.toJson(data)
                    sharedPreferences.commit { putString(profileKey, jsonString) }
                }

                else -> {
                    val beforeProfileInfoDTO = gson.fromJson(beforeData, ProfileInfoDTO::class.java)
                    if (beforeProfileInfoDTO == data) return@withContext true
                    val jsonString = gson.toJson(data)
                    sharedPreferences.commit { putString(profileKey, jsonString) }
                }
            }
            if (success) {
                profileInfoDTO = data
            }
            success
        }.getOrElse {
            it.printStackTrace()
            false
        }
    }

    override suspend fun getNickName(profileKey: String): ProfileInfoDTO? = withContext(ioDispatcher) {
        runCatching {
            profileInfoDTO?.let { data ->
                return@withContext data
            }
            val jsonString = sharedPreferences.getString(profileKey, null) ?: return@withContext null
            val profileInfoDTO = gson.fromJson(jsonString, ProfileInfoDTO::class.java)
            this@DataStoreImpl.profileInfoDTO = profileInfoDTO
            profileInfoDTO
        }.getOrElse {
            it.printStackTrace()
            null
        }
    }

    override suspend fun getUserInfo(key: String): UserInfoDTO? = withContext(ioDispatcher) {
        runCatching {
            userInfo?.let { data ->
                return@withContext data
            }
            val jsonString = sharedPreferences.getString(key, null) ?: return@withContext null
            val userInfoDTO = gson.fromJson(jsonString, UserInfoDTO::class.java)
            this@DataStoreImpl.userInfo = userInfoDTO
            userInfoDTO
        }.getOrElse {
            it.printStackTrace()
            null
        }
    }

    override suspend fun clearAllData(): Boolean = withContext(ioDispatcher) {
        runCatching {
            val success = sharedPreferences.commit { clear() }
            if (success) {
                cachedToken = null
                userInfo = null
                profileInfoDTO = null
            }
            success
        }.getOrElse {
            it.printStackTrace()
            false
        }
    }
}
