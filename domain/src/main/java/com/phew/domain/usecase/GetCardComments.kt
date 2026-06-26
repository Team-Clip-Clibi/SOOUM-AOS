package com.phew.domain.usecase

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.core_common.exception.asSooumException
import com.phew.domain.dto.CardComment
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.network.CardDetailRepository
import com.phew.core_common.log.SooumLog
import com.phew.core_common.resultFailure
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

    suspend operator fun invoke(param: Param): Result<List<CardComment>> {
        SooumLog.d(TAG, "GetCardComments() start cardId: ${param.cardId}")
        
        val locationPermissionCheck = deviceRepository.getLocationPermission()
        val (latitude, longitude) = if (locationPermissionCheck) {
            val location = deviceRepository.requestLocation()
            location.latitude to location.longitude
        } else {
            null to null
        }

        SooumLog.d(TAG, "GetCardComments() location: lat=$latitude, lng=$longitude")

        return repository.getCardComments(param.cardId, latitude, longitude).fold(
            onSuccess = { comments ->
                SooumLog.d(TAG, "GetCardComments() success: ${comments.size} comments")
                Result.success(comments)
            },
            onFailure = { throwable ->
                val exception = throwable.asSooumException()
                SooumLog.e(TAG, "GetCardComments() failed: code=${exception.code}, message=${exception.message}")
                
                if (exception.code == HTTP_NO_MORE_CONTENT) {
                    SooumLog.d(TAG, "GetCardComments() no comments available (HTTP 204)")
                    Result.success(emptyList())
                } else {
                    mapFailure(exception.code, exception.message, throwable)
                }
            },
        )
    }

    private fun mapFailure(code: Int, message: String, throwable: Throwable): Result<List<CardComment>> {
        return when (code) {
            HTTP_INVALID_TOKEN -> {
                SooumLog.e(TAG, "GetCardComments() invalid token")
                resultFailure(message = ERROR_LOGOUT, throwable = throwable)
            }
            APP_ERROR_CODE -> {
                SooumLog.e(TAG, "GetCardComments() app error: $message")
                resultFailure(message = message.ifBlank { ERROR_FAIL_JOB }, throwable = throwable)
            }
            else -> {
                SooumLog.e(TAG, "GetCardComments() network error - code: $code, message: $message")
                resultFailure(message = ERROR_NETWORK, throwable = throwable)
            }
        }
    }
}

private const val TAG = "GetCardComments"
