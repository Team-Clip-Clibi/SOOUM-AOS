package com.phew.device.device

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.Exception

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

    override suspend fun firebaseToken(): String {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            return token
        } catch (e: Exception) {
            e.printStackTrace()
            return "error"
        }
    }

}