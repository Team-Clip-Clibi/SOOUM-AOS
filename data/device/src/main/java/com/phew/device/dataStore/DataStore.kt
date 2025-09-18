package com.phew.device.dataStore


interface DataStore {

    suspend fun insertToken(key: String, data: Pair<String, String>): Boolean

    suspend fun getToken(key: String): Pair<String, String>

    suspend fun remove(key: String): Boolean
}