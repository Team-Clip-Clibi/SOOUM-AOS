package com.phew.domain.token

interface TokenManger {
    suspend fun getAccessToken(): String
    suspend fun getRefreshToken() : String
    suspend fun saveTokens(refreshToken: String, accessToken: String)
    suspend fun clearToken()
    suspend fun refreshAndGetNewToken(): String
}