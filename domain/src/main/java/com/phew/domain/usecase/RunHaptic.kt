package com.phew.domain.usecase

import com.phew.domain.repository.DeviceRepository
import javax.inject.Inject

class RunHaptic @Inject constructor(private val deviceRepository: DeviceRepository) {
    operator fun invoke() = deviceRepository.requestHaptic()
}