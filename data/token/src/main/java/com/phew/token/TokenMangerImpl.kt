package com.phew.token

import com.phew.core_common.DataResult
import com.phew.domain.dto.Token
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.NetworkRepository
import com.phew.domain.token.TokenManger
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class TokenMangerImpl @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val networkRepository: NetworkRepository,
) : TokenManger {
    private val mutex = Mutex()

    @Volatile
    private var isRefreshing = false

    @Volatile
    private var lastRefreshSuccess = false

    override suspend fun requestUpdateToken(data: Token): Boolean {
        if (isRefreshing) {
            return mutex.withLock { lastRefreshSuccess }
        }
        return mutex.withLock {
            if (isRefreshing) return@withLock lastRefreshSuccess
            isRefreshing = true
            val result = networkRepository.requestRefreshToken(data = data)
            lastRefreshSuccess = result is DataResult.Success && deviceRepository.saveToken(
                key = BuildConfig.TOKEN_KEY,
                data = result.data
            )
            isRefreshing = false
            lastRefreshSuccess
        }
    }
}