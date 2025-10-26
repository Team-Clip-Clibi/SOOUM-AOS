package com.phew.domain.usecase

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.domain.dto.CardComment
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.network.CardDetailRepository
import com.phew.core_common.log.SooumLog
import javax.inject.Inject


/**
 *  TODO Paging 처리 필요
 */
class GetCardComments @Inject constructor(
    private val repository: CardDetailRepository,
    private val deviceRepository: DeviceRepository
) {
    data class Param(
        val cardId: Long
    )

    suspend operator fun invoke(param: Param): DomainResult<List<CardComment>, String> {
        SooumLog.d(TAG, "GetCardComments() start cardId: ${param.cardId}")
        
        val locationPermissionCheck = deviceRepository.getLocationPermission()
        val (latitude, longitude) = if (locationPermissionCheck) {
            val location = deviceRepository.requestLocation()
            location.latitude to location.longitude
        } else {
            null to null
        }

        SooumLog.d(TAG, "GetCardComments() location: lat=$latitude, lng=$longitude")

        return when (val result = repository.getCardComments(param.cardId, latitude, longitude)) {
            is DataResult.Success -> {
                SooumLog.d(TAG, "GetCardComments() success: ${result.data.size} comments")
                DomainResult.Success(result.data)
            }
            is DataResult.Fail -> {
                SooumLog.e(TAG, "GetCardComments() failed: code=${result.code}, message=${result.message}")
                
                // 204 No Content는 댓글이 없다는 의미이므로 빈 리스트로 성공 처리
                if (result.code == HTTP_NO_MORE_CONTENT) {
                    SooumLog.d(TAG, "GetCardComments() no comments available (HTTP 204)")
                    DomainResult.Success(emptyList())
                } else {
                    mapFailure(result)
                }
            }
        }
    }

    private fun mapFailure(result: DataResult.Fail): DomainResult.Failure<String> {
        return when (result.code) {
            HTTP_INVALID_TOKEN -> {
                SooumLog.e(TAG, "GetCardComments() invalid token")
                DomainResult.Failure(ERROR_LOGOUT)
            }
            APP_ERROR_CODE -> {
                SooumLog.e(TAG, "GetCardComments() app error: ${result.message}")
                DomainResult.Failure(result.message ?: ERROR_FAIL_JOB)
            }
            else -> {
                SooumLog.e(TAG, "GetCardComments() network error - code: ${result.code}, message: ${result.message}")
                DomainResult.Failure(ERROR_NETWORK)
            }
        }
    }
}

private const val TAG = "GetCardComments"
