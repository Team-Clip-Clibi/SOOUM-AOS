package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.domain.BuildConfig
import com.phew.domain.model.AppVersionStatusType
import com.phew.domain.repository.network.SplashRepository
import javax.inject.Inject

class CheckAppVersion @Inject constructor(private val repository: SplashRepository) {
    data class Param(
        val appVersion: String,
        val isDebugMode: Boolean,
    )

    suspend operator fun invoke(data: Param): DomainResult<AppVersionStatusType, Unit> {
        if (data.isDebugMode) {
            return DomainResult.Success(AppVersionStatusType.OK)
        }
        val version = data.appVersion.substringBefore("-")
        val result = repository.requestAppVersion(
            type = BuildConfig.APP_TYPE,
            appVersion = version
        )
        when (result) {
            is DataResult.Fail -> {
                return DomainResult.Failure(Unit)
            }

            is DataResult.Success -> {
                if (result.data.status == AppVersionStatusType.UPDATE) {
                    return DomainResult.Success(AppVersionStatusType.UPDATE)
                }
                if (result.data.latestVersion != version) {
                    return DomainResult.Success(AppVersionStatusType.PENDING)
                }
                return DomainResult.Success(AppVersionStatusType.OK)
            }
        }
    }
}