package com.momid.kanda333

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.momid.kanda333.database.data_model.LocationData
import com.momid.kanda333.view_model.LocationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

var locationCallback: LocationCallback? = null

fun startLocationUpdates(
    fusedLocationClient: FusedLocationProviderClient,
    isTracking: Boolean,
    locationViewModel: LocationViewModel,
    context: Context,
    onLocationResult: (LatLng) -> Unit
) {
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()

    locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                if (isTracking) {
                    CoroutineScope(Dispatchers.IO).launch {
                        locationViewModel.insertLocation(
                            LocationData(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
                onLocationResult(LatLng(location.latitude, location.longitude))
            }
        }
    }

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    fusedLocationClient.requestLocationUpdates(
        locationRequest,
        locationCallback!!,
        Looper.getMainLooper()
    )
}

fun lastLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationAvailable: (LatLng) -> Unit
) {
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
        return
    }

    fusedLocationClient.lastLocation.addOnSuccessListener {
        onLocationAvailable(LatLng(it.latitude, it.longitude))
    }
}


fun stopLocationUpdates(fusedLocationClient: FusedLocationProviderClient) {
    locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
}

fun createCsvFile(locations: List<LocationData>): String {
    val csvContent = StringBuilder()
    csvContent.append("Latitude,Longitude,Timestamp\n")

    locations.forEach { location ->
        val formattedTimestamp =
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            ).format(Date(location.timestamp))
        csvContent.append("${location.latitude},${location.longitude},$formattedTimestamp\n")
    }
    return csvContent.toString()
}
