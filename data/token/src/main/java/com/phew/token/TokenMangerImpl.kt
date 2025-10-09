package com.phew.token

import com.phew.domain.dto.Token
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.token.TokenManger
import com.phew.network.dto.TokenDTO
import com.phew.network.retrofit.TokenRefreshHttp
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class TokenMangerImpl @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val tokenRefreshApi: TokenRefreshHttp
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
            if(cachedAccessToken.isEmpty()) {
                val token = deviceRepository.requestToken(BuildConfig.TOKEN_KEY)
                cachedAccessToken = token.second
            }
        }
        return cachedAccessToken
    }

    override suspend fun getRefreshToken(): String {
        if (cachedRefreshToken.isNotEmpty()) return cachedRefreshToken
        cacheMutex.withLock {
            if(cachedRefreshToken.isEmpty()) {
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
            if (response.isSuccessful && response.body() != null) {
                val newToken = response.body()!!
                saveTokens(newToken.refreshToken, newToken.accessToken)
                newToken.accessToken
            } else {
                clearToken()
                ""
            }
        }
    }
}