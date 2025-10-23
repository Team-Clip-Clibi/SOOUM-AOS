package com.phew.location_provider

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.phew.location_provider.dto.LocationDTO
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LocationProviderImpl @Inject constructor(@ApplicationContext private val context: Context) :
    LocationProvider {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    override suspend fun locationPermissionCheck(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun isLocationEnable(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

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