package com.phew.location_provider

import com.phew.location_provider.dto.LocationDTO

interface LocationProvider {
    suspend fun locationPermissionCheck(): Boolean
    suspend fun isLocationEnable(): Boolean
    suspend fun location(): LocationDTO
}