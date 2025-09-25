package com.phew.domain.token

interface TokenManger {
    suspend fun requestUpdateToken(refreshToken: String): Boolean
}