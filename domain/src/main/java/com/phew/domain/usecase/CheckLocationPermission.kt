package com.phew.domain.usecase

import com.phew.domain.BuildConfig
import com.phew.domain.repository.DeviceRepository
import javax.inject.Inject

class CheckLocationPermission @Inject constructor(private val deviceRepository: DeviceRepository) {
    suspend operator fun invoke(): Boolean {
        return deviceRepository.requestGetLocationPermissionIsAsk(BuildConfig.LOCATION_KEY)
    }
}