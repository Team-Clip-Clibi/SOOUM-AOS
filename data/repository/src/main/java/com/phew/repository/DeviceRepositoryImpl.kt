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
}