package com.phew.device.device

import com.phew.device.dto.LocationDTO

interface Device {
    suspend fun deviceId(): String
    suspend fun firebaseToken(): String
    suspend fun location(): LocationDTO
}