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

        val result = repository.requestAppVersion(
            type = BuildConfig.APP_TYPE,
            appVersion = data.appVersion.substringBefore("-")
        )
        return when (result) {
            is DataResult.Fail -> {
                DomainResult.Failure(Unit)
            }

            is DataResult.Success -> {
                DomainResult.Success(result.data.status)
            }
        }
    }
}