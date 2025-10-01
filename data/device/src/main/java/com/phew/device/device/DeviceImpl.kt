package com.phew.device.device

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.annotation.RequiresPermission
import com.google.firebase.messaging.FirebaseMessaging
import com.phew.core_common.ERROR
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import androidx.core.content.ContextCompat
import com.phew.device.dto.LocationDTO
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.Exception

class DeviceImpl @Inject constructor(@ApplicationContext private val context: Context) :
    Device {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

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
            return ERROR
        }
    }


    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override suspend fun location(): LocationDTO {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasCoarseLocationPermission && hasFineLocationPermission) {
            return LocationDTO(latitude = null, longitude = null)
        }
        val priority =
            if (hasFineLocationPermission) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_LOW_POWER
        try {
            val location = fusedLocationClient.getCurrentLocation(priority, null).await()
            return LocationDTO(latitude = location.latitude, longitude = location.longitude)
        } catch (e: SecurityException) {
            e.printStackTrace()
            return LocationDTO(latitude = null, longitude = null)
        } catch (e: Exception) {
            e.printStackTrace()
            return LocationDTO(latitude = null, longitude = null)
        }
    }

}