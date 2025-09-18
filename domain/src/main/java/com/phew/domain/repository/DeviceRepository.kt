package com.phew.domain.repository

interface DeviceRepository {
    suspend fun requestDeviceId(): String
    suspend fun requestToken(key: String): Pair<String, String>
    suspend fun saveToken(key: String, data: Pair<String, String>): Boolean
    suspend fun firebaseToken(): String
    suspend fun requestGetSaveFirebaseToken(key: String): String
    suspend fun requestSaveFirebaseToken(key: String, data: String): Boolean
    suspend fun requestSaveNotify(key: String, data: Boolean): Boolean
    suspend fun requestGetNotify(key: String): Boolean
}