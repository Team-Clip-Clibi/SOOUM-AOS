package com.phew.device.device

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.Exception
import javax.inject.Inject

class DeviceImpl @Inject constructor(@ApplicationContext private val context: Context) :
    Device {
    @SuppressLint("HardwareIds")
    override suspend fun deviceId(): String {
        try {
            val id = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            return id
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalArgumentException("Device ID not fount ${e.message}")
        }
    }
}