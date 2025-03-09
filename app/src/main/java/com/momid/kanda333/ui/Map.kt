package com.momid.kanda333.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.momid.kanda333.createCsvFile
import com.momid.kanda333.lastLocation
import com.momid.kanda333.startLocationUpdates
import com.momid.kanda333.stopLocationUpdates
import com.momid.kanda333.view_model.AuthViewModel
import com.momid.kanda333.view_model.LocationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.util.Date

@Composable
fun MapScreen(
    authViewModel: AuthViewModel,
    locationViewModel: LocationViewModel,
    fusedLocationClient: FusedLocationProviderClient
) {
    val context = LocalContext.current
    var isTracking by remember { mutableStateOf(false) }
    val locations by locationViewModel.locations.collectAsState(initial = emptyList())
    var currentLatLng by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLatLng, 15f)
    }

    var hasLocationPermission by remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions.all { it.value }

        if (hasLocationPermission) {
            Log.d("MapScreen", "Permissions granted")
            startLocationUpdates(
                fusedLocationClient,
                isTracking,
                locationViewModel,
                context
            ) { latLng ->
                currentLatLng = latLng
                cameraPositionState.move(CameraUpdateFactory.newLatLng(latLng))
            }
        } else {
            Log.d("MapScreen", "Permissions denied")
            Toast.makeText(context, "Location permissions denied", Toast.LENGTH_SHORT).show()
        }
    }


    SideEffect {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        hasLocationPermission = fineLocationGranted && coarseLocationGranted

        if (!hasLocationPermission) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            startLocationUpdates(
                fusedLocationClient,
                isTracking,
                locationViewModel,
                context
            ) { latLng ->
                currentLatLng = latLng
                cameraPositionState.move(CameraUpdateFactory.newLatLng(latLng))
            }
        }
    }

    val uiSettings by remember { mutableStateOf(MapUiSettings(myLocationButtonEnabled = hasLocationPermission)) }
    val properties by remember {
        mutableStateOf(MapProperties(isMyLocationEnabled = hasLocationPermission))
    }

    lastLocation(context, fusedLocationClient) {
        currentLatLng = it
        cameraPositionState.move(CameraUpdateFactory.newLatLng(it))
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let {
            try {
                val outputStream: OutputStream? = context.contentResolver.openOutputStream(it)
                outputStream?.use { stream ->
                    stream.write(createCsvFile(locations).toByteArray())
                }
                Toast.makeText(context, "File saved successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error saving file: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("MapScreen", "File save error", e)
            }
        }
    }
    Column(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.weight(1f),
            cameraPositionState = cameraPositionState,
            properties = properties,
            uiSettings = uiSettings
        ) {
            locations.forEach { location ->
                Marker(
                    state = MarkerState(position = LatLng(location.latitude, location.longitude)),
                    title = "Tracked Location",
                    snippet = "Time: ${Date(location.timestamp)}"
                )
            }

            Polyline(
                points = locations.map { LatLng(it.latitude, it.longitude) },
                color = MaterialTheme.colorScheme.primary
            )

            Marker(
                state = MarkerState(position = currentLatLng),
                title = "You",
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (!isTracking) {
                Button(onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        locationViewModel.clearLocations()
                    }.invokeOnCompletion {
                        isTracking = true
                    }
                }) {
                    Text("Start")
                }
            } else {
                Button(onClick = {
                    isTracking = false
                    stopLocationUpdates(fusedLocationClient)
                }) {
                    Text("Stop")
                }
            }

            if (!isTracking && locations.isNotEmpty()) {
                Button(onClick = {
                    createDocumentLauncher.launch("locations_${System.currentTimeMillis()}.csv")
                }) {
                    Text("Download Output")
                }
            }

            Button(onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    locationViewModel.clearLocations()
                }
                authViewModel.logout()
            }) {
                Text("Logout")
            }
        }
    }
}
