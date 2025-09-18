package com.phew.domain.repository

interface DeviceRepository {
    suspend fun requestDeviceId(): String
    suspend fun requestToken(key: String): Pair<String, String>
}