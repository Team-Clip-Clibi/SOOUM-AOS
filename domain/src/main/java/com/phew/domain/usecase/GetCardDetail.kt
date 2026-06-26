package com.phew.domain.usecase

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.ERROR_ALREADY_CARD_DELETE
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_CARD_ALREADY_DELETE
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.mapFailureMessage
import com.phew.domain.dto.CardDetail
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.network.CardDetailRepository
import javax.inject.Inject

class GetCardDetail @Inject constructor(
    private val repository: CardDetailRepository,
    private val deviceRepository: DeviceRepository,
) {
    data class Param(
        val cardId: Long
    )

    suspend operator fun invoke(param: Param): Result<CardDetail> {
        val locationPermissionCheck = deviceRepository.getLocationPermission()
        val (latitude, longitude) = if (locationPermissionCheck) {
            val location = deviceRepository.requestLocation()
            location.latitude to location.longitude
        } else {
            null to null
        }

        return repository.getCardDetail(param.cardId, latitude, longitude).mapFailureMessage { code, message ->
            when (code) {
                HTTP_INVALID_TOKEN -> ERROR_LOGOUT
                APP_ERROR_CODE -> message.ifBlank { ERROR_FAIL_JOB }
                HTTP_CARD_ALREADY_DELETE -> ERROR_ALREADY_CARD_DELETE
                else -> ERROR_NETWORK
            }
        }
    }
}
