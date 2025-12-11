package com.phew.domain.usecase

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.log.SooumLog
import com.phew.domain.BuildConfig
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.network.MembersRepository
import javax.inject.Inject

class ToggleNotification @Inject constructor(
    private val membersRepository: MembersRepository,
    private val deviceRepository: DeviceRepository
) {
    suspend operator fun invoke(isAllowNotify: Boolean? = null): DomainResult<Boolean, String> {
        return try {
            val currentCachedStatus = deviceRepository.requestGetNotify(BuildConfig.NOTIFY_KEY)
            val desiredStatus = isAllowNotify ?: currentCachedStatus

            when (val result = membersRepository.toggleNotification(desiredStatus)) {
                is DataResult.Success -> {
                    // 서버 동기화 성공 시 로컬에 저장
                    val saveResult = deviceRepository.requestSaveNotify(
                        key = BuildConfig.NOTIFY_KEY,
                        data = desiredStatus
                    )
                    if (!saveResult) {
                        SooumLog.e(TAG, "Failed to save notification state to local storage")
                        return DomainResult.Failure(ERROR_FAIL_JOB)
                    }
                    DomainResult.Success(desiredStatus)
                }
                is DataResult.Fail -> {
                    // isAllowNotify가 null인 경우(상태 조회): 네트워크 실패해도 로컬 상태 반환
                    if (isAllowNotify == null) {
                        SooumLog.w(TAG, "Network sync failed, returning cached status: $currentCachedStatus")
                        DomainResult.Success(currentCachedStatus)
                    } else {
                        // isAllowNotify가 설정된 경우(상태 변경): 네트워크 오류 반환
                        mapFailure(result)
                    }
                }
            }
        } catch (e: Exception) {
            // isAllowNotify가 null인 경우는 로컬 상태 반환, 아니면 에러 반환
            if (isAllowNotify == null) {
                val currentCachedStatus = deviceRepository.requestGetNotify(BuildConfig.NOTIFY_KEY)
                SooumLog.w(TAG, "Exception during sync, returning cached status: $currentCachedStatus")
                DomainResult.Success(currentCachedStatus)
            } else {
                SooumLog.e(TAG, "Exception during notification toggle: ${e.message}")
                DomainResult.Failure(e.message ?: ERROR_NETWORK)
            }
        }
    }

    private fun mapFailure(result: DataResult.Fail): DomainResult<Boolean, String> {
        return when (result.code) {
            HTTP_INVALID_TOKEN -> DomainResult.Failure(ERROR_LOGOUT)
            APP_ERROR_CODE -> DomainResult.Failure(result.message ?: ERROR_FAIL_JOB)
            else -> DomainResult.Failure(result.message ?: ERROR_NETWORK)
        }
    }
    
    companion object {
        private const val TAG = "ToggleNotification"
    }
}
