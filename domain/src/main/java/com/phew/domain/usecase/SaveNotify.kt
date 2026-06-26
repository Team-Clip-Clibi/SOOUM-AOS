package com.phew.domain.usecase

import com.phew.domain.BuildConfig
import com.phew.domain.repository.DeviceRepository
import javax.inject.Inject

class SaveNotify @Inject constructor(private val deviceRepository: DeviceRepository) {
    data class Param(
        val status: Boolean
    )

    suspend operator fun invoke(data: Param): Result<Unit> {
        val request =
            deviceRepository.requestSaveNotify(key = BuildConfig.NOTIFY_KEY, data = data.status)
        if (!request) {
            return com.phew.core_common.resultFailure(Unit)
        }
        return Result.success(Unit)
    }
}