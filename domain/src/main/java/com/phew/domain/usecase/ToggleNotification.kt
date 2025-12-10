package com.phew.domain.usecase

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.DataResult
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.domain.BuildConfig
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.network.MembersRepository
import javax.inject.Inject

class ToggleNotification @Inject constructor(
    private val membersRepository: MembersRepository,
    private val deviceRepository: DeviceRepository
) {
    suspend operator fun invoke(isAllowNotify: Boolean? = null): DataResult<Boolean> {
        return try {
            val currentStatus = deviceRepository.requestGetNotify(BuildConfig.NOTIFY_KEY)
            val desiredStatus = isAllowNotify ?: currentStatus

            when (val result = membersRepository.toggleNotification(desiredStatus)) {
                is DataResult.Success -> {
                    val saveResult = deviceRepository.requestSaveNotify(
                        key = BuildConfig.NOTIFY_KEY,
                        data = desiredStatus
                    )
                    if (!saveResult) {
                        return mapFailure(
                            DataResult.Fail(
                                code = APP_ERROR_CODE,
                                message = ERROR_FAIL_JOB
                            )
                        )
                    }
                    DataResult.Success(desiredStatus)
                }
                is DataResult.Fail -> mapFailure(result)
            }
        } catch (e: Exception) {
            mapFailure(DataResult.Fail(message = e.message, throwable = e))
        }
    }

    private fun mapFailure(result: DataResult.Fail): DataResult<Boolean> {
        return when (result.code) {
            HTTP_INVALID_TOKEN -> DataResult.Fail(
                code = result.code,
                message = ERROR_LOGOUT,
                throwable = result.throwable
            )
            APP_ERROR_CODE -> DataResult.Fail(
                code = result.code,
                message = result.message ?: ERROR_FAIL_JOB,
                throwable = result.throwable
            )
            else -> DataResult.Fail(
                code = result.code,
                message = result.message ?: ERROR_NETWORK,
                throwable = result.throwable
            )
        }
    }
}
