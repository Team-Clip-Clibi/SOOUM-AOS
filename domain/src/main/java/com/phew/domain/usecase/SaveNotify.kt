package com.phew.domain.usecase

import com.phew.core_common.DomainResult
import com.phew.domain.BuildConfig
import com.phew.domain.repository.DeviceRepository
import javax.inject.Inject

class SaveNotify @Inject constructor(private val deviceRepository: DeviceRepository) {
    data class Param(
        val status: Boolean
    )

    suspend operator fun invoke(data: Param): DomainResult<Unit, Unit> {
        val request =
            deviceRepository.requestSaveNotify(key = BuildConfig.NOTIFY_KEY, data = data.status)
        if (!request) {
            return DomainResult.Failure(Unit)
        }
        return DomainResult.Success(Unit)
    }
}