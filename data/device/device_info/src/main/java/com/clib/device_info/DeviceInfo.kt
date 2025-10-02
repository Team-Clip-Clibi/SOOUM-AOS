package com.clib.device_info

interface DeviceInfo {
    suspend fun deviceId(): String
    suspend fun firebaseToken(): String
}