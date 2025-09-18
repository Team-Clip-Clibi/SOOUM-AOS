package com.phew.repository

import com.phew.device.dataStore.DataStore
import com.phew.device.device.Device
import com.phew.domain.repository.DeviceRepository
import javax.inject.Inject


class DeviceRepositoryImpl @Inject constructor(
    private val device: Device,
    private val dataSource: DataStore,
) : DeviceRepository {
    override suspend fun requestDeviceId(): String {
        return device.deviceId()
    }

    override suspend fun requestToken(key: String): Pair<String, String> {
        return dataSource.getToken(key)
    }

    override suspend fun saveToken(key: String, data: Pair<String, String>): Boolean {
        return dataSource.insertToken(key = key, data = data)
    }

    override suspend fun firebaseToken(): String {
        return device.firebaseToken()
    }

    override suspend fun requestGetSaveFirebaseToken(key: String): String {
        return dataSource.getFirebaseToken(key)
    }

    override suspend fun requestSaveFirebaseToken(key: String, data: String): Boolean {
        return dataSource.insertFirebaseToken(key = key, data = data)
    }
}