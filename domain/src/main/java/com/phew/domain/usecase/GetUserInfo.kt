package com.phew.domain.usecase

import com.phew.domain.dto.UserInfo
import com.phew.domain.repository.DeviceRepository
import javax.inject.Inject

class GetUserInfo @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    data class Param(
        val key: String
    )

    suspend operator fun invoke(param: Param): UserInfo? {
        return deviceRepository.getUserInfo(param.key)
    }
}