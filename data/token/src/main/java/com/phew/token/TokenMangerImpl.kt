package com.phew.token

import com.phew.domain.dto.Token
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.token.TokenManger
import com.phew.network.dto.InfoDTO
import com.phew.network.dto.TokenDTO
import com.phew.network.retrofit.TokenRefreshHttp
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.inject.Inject

class TokenMangerImpl @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val tokenRefreshApi: TokenRefreshHttp,
) : TokenManger {

    private val cacheMutex = Mutex()
    private val refreshMutex = Mutex()

    @Volatile
    private var cachedAccessToken: String = ""

    @Volatile
    private var cachedRefreshToken: String = ""

    override suspend fun getAccessToken(): String {
        if (cachedAccessToken.isNotEmpty()) return cachedAccessToken
        cacheMutex.withLock {
            if (cachedAccessToken.isEmpty()) {
                val token = deviceRepository.requestToken(BuildConfig.TOKEN_KEY)
                cachedAccessToken = token.second
            }
        }
        return cachedAccessToken
    }

    override suspend fun getRefreshToken(): String {
        if (cachedRefreshToken.isNotEmpty()) return cachedRefreshToken
        cacheMutex.withLock {
            if (cachedRefreshToken.isEmpty()) {
                val token = deviceRepository.requestToken(BuildConfig.TOKEN_KEY)
                cachedRefreshToken = token.first
            }
        }
        return cachedRefreshToken
    }

    override suspend fun saveTokens(refreshToken: String, accessToken: String) {
        cacheMutex.withLock {
            cachedRefreshToken = refreshToken
            cachedAccessToken = accessToken
        }
        deviceRepository.saveToken(
            BuildConfig.TOKEN_KEY,
            Token(refreshToken = refreshToken, accessToken = accessToken)
        )
    }

    override suspend fun clearToken() {
        cacheMutex.withLock {
            cachedRefreshToken = ""
            cachedAccessToken = ""
        }
        deviceRepository.deleteDataStoreInfo(BuildConfig.TOKEN_KEY)
    }

    override suspend fun refreshAndGetNewToken(): String {
        val oldAccessToken = getAccessToken()
        return refreshMutex.withLock {
            val currentAccessToken = getAccessToken()
            if (oldAccessToken != currentAccessToken) {
                return@withLock currentAccessToken
            }
            val refreshToken = getRefreshToken()
            if (refreshToken.isEmpty()) {
                clearToken()
                return@withLock ""
            }
            val response = tokenRefreshApi.requestRefreshToken(
                body = TokenDTO(refreshToken, oldAccessToken)
            )
            if (!response.isSuccessful || response.body() == null) return@withLock ""
            val newToken = response.body()!!
            saveTokens(refreshToken = newToken.refreshToken, accessToken = newToken.accessToken)
            return newToken.accessToken
        }
    }

    override suspend fun autoLogin(): String {
        val securityKeyResponse = tokenRefreshApi.getSecurityKey()
        val securityKeyBody = securityKeyResponse.body()
        if (!securityKeyResponse.isSuccessful || securityKeyBody == null) return ""
        val key = makeSecurityKey(securityKeyBody.publicKey)
        val deviceId = deviceRepository.requestDeviceId()
        val deviceOs = deviceRepository.requestDeviceOS()
        val deviceModel = deviceRepository.requestDeviceModel()
        val encryptData = encrypt(data = deviceId, key = key)
        val requestLogin = tokenRefreshApi.requestLogin(
            InfoDTO(
                encryptedDeviceId = encryptData,
                deviceType = "ANDROID",
                deviceOsVersion = deviceOs,
                deviceModel = deviceModel
            )
        )
        val loginBody = requestLogin.body()
        if (!requestLogin.isSuccessful || loginBody == null) return ""
        val data = requestLogin.body() ?: return ""
        saveTokens(refreshToken = loginBody.refreshToken, accessToken = loginBody.accessToken)
        return data.accessToken
    }

    private fun makeSecurityKey(key: String): PublicKey {
        val cleanedKey = key.replace("\\s".toRegex(), "")
        val keyBytes = java.util.Base64.getDecoder().decode(cleanedKey)
        val spec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(spec)
    }

    private fun encrypt(data: String, key: PublicKey): String {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        return java.util.Base64.getEncoder().encodeToString(encryptedBytes)
    }
}