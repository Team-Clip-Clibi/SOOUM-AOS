package com.phew.device_info

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import com.google.firebase.messaging.FirebaseMessaging
import com.phew.core_common.ERROR
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DeviceInfoImpl @Inject constructor(@ApplicationContext private val context : Context)  : DeviceInfo {
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

    override suspend fun osVersion(): String {
        try {
            return android.os.Build.VERSION.RELEASE
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalArgumentException("Device OS Version not fount ${e.message}")
        }
    }

    override suspend fun modelName(): String {
        try {
            return android.os.Build.MODEL
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalArgumentException("Device Model Name not fount ${e.message}")
        }
    }

    override suspend fun firebaseToken(): String {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            return token
        } catch (e: Exception) {
            e.printStackTrace()
            return ERROR
        }
    }
}