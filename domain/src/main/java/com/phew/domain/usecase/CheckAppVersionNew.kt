package com.phew.domain.usecase

import com.phew.core_common.DomainResult
import com.phew.domain.model.AppVersionStatus
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.network.AppVersionRepository
import javax.inject.Inject

class CheckAppVersionNew @Inject constructor(
    private val appVersionRepository: AppVersionRepository,
    private val deviceRepository: DeviceRepository
) {
    data class Param(
        val type: String
    )

    suspend operator fun invoke(param: Param): DomainResult<AppVersionStatus, Unit> {
        return try {
            val appVersion = deviceRepository.getAppVersion()
            val result = appVersionRepository.checkAppVersion(param.type, appVersion)
            result.fold(
                onSuccess = { status ->
                    DomainResult.Success(status)
                },
                onFailure = {
                    DomainResult.Failure(Unit)
                }
            )
        } catch (e: Exception) {
            DomainResult.Failure(Unit)
        }
    }
}