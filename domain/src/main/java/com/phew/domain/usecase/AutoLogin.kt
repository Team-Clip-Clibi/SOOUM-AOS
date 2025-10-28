package com.phew.domain.usecase

import com.phew.core_common.ERROR_NO_DATA
import com.phew.domain.BuildConfig
import com.phew.domain.repository.DeviceRepository
import javax.inject.Inject

class AutoLogin @Inject constructor(private val deviceRepository: DeviceRepository) {
    suspend operator fun invoke(): Boolean {
        val token = deviceRepository.requestToken(BuildConfig.TOKEN_KEY)
        return !(token.first == ERROR_NO_DATA || token.second == ERROR_NO_DATA)
    }
}