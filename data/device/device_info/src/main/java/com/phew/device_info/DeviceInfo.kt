package com.phew.device_info

interface DeviceInfo {
    suspend fun deviceId(): String
    suspend fun osVersion(): String
    suspend fun modelName(): String
    suspend fun firebaseToken(): String
    suspend fun appVersion(): String
}