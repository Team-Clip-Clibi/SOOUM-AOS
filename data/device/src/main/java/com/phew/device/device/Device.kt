package com.phew.device.device

interface Device {
    suspend fun deviceId(): String
    suspend fun firebaseToken() : String
}