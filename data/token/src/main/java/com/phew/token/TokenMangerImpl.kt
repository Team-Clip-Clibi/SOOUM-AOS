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
    private val mutex = Mutex()

    override suspend fun getAccessToken(): String {
        val token = deviceRepository.requestToken(BuildConfig.TOKEN_KEY)
        return token.second
    }

    override suspend fun getRefreshToken(): String {
        val token = deviceRepository.requestToken(BuildConfig.TOKEN_KEY)
        return token.first
    }

    override suspend fun saveTokens(refreshToken: String, accessToken: String) {
        deviceRepository.saveToken(
            BuildConfig.TOKEN_KEY,
            Token(refreshToken = refreshToken, accessToken = accessToken)
        )
    }

    override suspend fun clearToken() {
        deviceRepository.deleteDataStoreInfo(BuildConfig.TOKEN_KEY)
    }

    override suspend fun refreshAndGetNewToken(): String? {
        val oldAccessToken = getAccessToken()

        return mutex.withLock {
            val currentAccessToken = getAccessToken()
            if (oldAccessToken != currentAccessToken) {
                return@withLock currentAccessToken
            }
            val refreshToken = getRefreshToken()
            if (refreshToken.isEmpty()) {
                return@withLock null
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
                null
            }
        }
    }
}