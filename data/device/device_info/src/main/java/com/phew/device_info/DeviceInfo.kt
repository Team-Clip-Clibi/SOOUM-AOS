package com.phew.device_info

interface DeviceInfo {
    suspend fun deviceId(): String
    suspend fun firebaseToken(): String
}