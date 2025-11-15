package com.phew.domain.usecase

import com.phew.core_common.ERROR_NO_DATA
import com.phew.domain.BuildConfig
import com.phew.domain.repository.DeviceRepository
import javax.inject.Inject

class GetRefreshToken @Inject constructor(
    private val deviceRepository: DeviceRepository
) {

    suspend operator fun invoke(): String {
        val token = deviceRepository.requestToken(BuildConfig.TOKEN_KEY)
        return token.first.takeUnless { it == ERROR_NO_DATA } ?: ""
    }
}
