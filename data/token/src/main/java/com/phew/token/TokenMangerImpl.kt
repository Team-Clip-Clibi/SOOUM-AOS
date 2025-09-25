package com.phew.token

import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.NetworkRepository
import com.phew.domain.token.TokenManger
import javax.inject.Inject

class TokenMangerImpl @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val networkRepository: NetworkRepository,
) : TokenManger {
    override suspend fun requestUpdateToken(refreshToken: String): Boolean {
        TODO("Not yet implemented")
    }
}