package com.phew.domain.usecase

import com.phew.domain.repository.DeviceRepository
import javax.inject.Inject

class GetProfileInfo @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    suspend operator fun invoke(profileKey: String): String? {
        return deviceRepository.getProfileInfo(profileKey)
    }
}
