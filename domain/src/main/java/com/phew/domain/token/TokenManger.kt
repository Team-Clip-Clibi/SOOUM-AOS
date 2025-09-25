package com.phew.domain.token

import com.phew.domain.dto.Token

interface TokenManger {
    suspend fun requestUpdateToken(data: Token): Boolean
}