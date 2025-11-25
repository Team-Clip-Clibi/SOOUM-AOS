package com.phew.domain.interceptor

interface InterceptorManger {
    suspend fun getAccessToken(): String
    suspend fun getRefreshToken(): String
    suspend fun saveTokens(refreshToken: String, accessToken: String)
    suspend fun clearToken()
    suspend fun refreshAndGetNewToken(): String
    suspend fun autoLogin(): String
    suspend fun deleteAll(): Boolean
}