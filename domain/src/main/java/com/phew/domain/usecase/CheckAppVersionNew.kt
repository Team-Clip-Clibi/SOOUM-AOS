package com.phew.domain.usecase

import com.phew.domain.model.AppVersionStatus
import com.phew.domain.model.AppVersionStatusType
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.network.AppVersionRepository
import javax.inject.Inject

class CheckAppVersionNew @Inject constructor(
    private val appVersionRepository: AppVersionRepository,
    private val deviceRepository: DeviceRepository
) {
    data class Param(
        val type: String,
        val isDebugMode: Boolean,
    )

    suspend operator fun invoke(param: Param): Result<AppVersionStatus> {
        return try {
            if (param.isDebugMode) {
                return Result.success(
                    AppVersionStatus(
                        status = AppVersionStatusType.OK,
                        latestVersion = "1.0.0"
                    )
                )
            }
            val appVersion = deviceRepository.getAppVersion()
            val result = appVersionRepository.checkAppVersion(param.type, appVersion)
            result.fold(
                onSuccess = { status ->
                    Result.success(status)
                },
                onFailure = {
                    com.phew.core_common.resultFailure(Unit)
                }
            )
        } catch (e: Exception) {
            com.phew.core_common.resultFailure(Unit)
        }
    }
}