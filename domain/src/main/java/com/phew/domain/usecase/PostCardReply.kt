package com.phew.domain.usecase

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_ACCOUNT_SUSPENDED
import com.phew.core_common.ERROR_ALREADY_CARD_DELETE
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.HTTP_BAD_REQUEST
import com.phew.core_common.HTTP_CARD_ALREADY_DELETE
import com.phew.domain.dto.CardReplyRequest
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.network.CardDetailRepository
import javax.inject.Inject

class PostCardReply @Inject constructor(
    private val repository: CardDetailRepository,
    private val deviceRepository: DeviceRepository
) {
    data class Param(
        val cardId: Long,
        val content: String,
        val font: String,
        val imgType: String,
        val imgName: String,
        val tags: List<String>,
        val isDistanceShared: Boolean
    )

    suspend operator fun invoke(param: Param): DomainResult<Long, String> {
        val locationPermissionCheck = deviceRepository.getLocationPermission()
        val (latitude, longitude) = if (locationPermissionCheck && param.isDistanceShared) {
            val location = deviceRepository.requestLocation()
            location.latitude to location.longitude
        } else {
            null to null
        }

        val request = CardReplyRequest(
            isDistanceShared = param.isDistanceShared,
            latitude = latitude,
            longitude = longitude,
            content = param.content,
            font = param.font,
            imgType = param.imgType,
            imgName = param.imgName,
            tags = param.tags
        )

        return when (val result = repository.postCardReply(param.cardId, request)) {
            is DataResult.Success -> DomainResult.Success(result.data.cardId)
            is DataResult.Fail -> mapFailure(result)
        }
    }

    private fun mapFailure(result: DataResult.Fail): DomainResult.Failure<String> {
        return when (result.code) {
            APP_ERROR_CODE -> DomainResult.Failure(result.message ?: ERROR_FAIL_JOB)
            HTTP_BAD_REQUEST -> DomainResult.Failure(ERROR_ACCOUNT_SUSPENDED)
            HTTP_CARD_ALREADY_DELETE -> DomainResult.Failure(ERROR_ALREADY_CARD_DELETE)
            else -> DomainResult.Failure(ERROR_FAIL_JOB)
        }
    }
}
