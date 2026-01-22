package com.phew.domain.usecase

import androidx.paging.PagingData
import com.phew.domain.dto.CardComment
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.PagerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCardCommentsPaging @Inject constructor(
    private val repository: PagerRepository,
    private val deviceRepository: DeviceRepository
) {
    data class Param(
        val cardId: Long
    )

    suspend operator fun invoke(param: Param): Flow<PagingData<CardComment>> {

        val locationPermissionCheck = deviceRepository.getLocationPermission()
        val (latitude, longitude) = if (locationPermissionCheck) {
            val location = deviceRepository.requestLocation()
            location.latitude to location.longitude
        } else {
            null to null
        }

        return repository.cardComments(
            cardId = param.cardId,
            latitude = latitude,
            longitude = longitude
        )
    }

}
