package com.example.group21

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



@Composable
fun getUserLocation(): State<LatLng?> {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val locationState = remember { mutableStateOf<LatLng?>(null) }
    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationRequest = remember {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100).build()
    }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    locationState.value = LatLng(location.latitude, location.longitude)
                }
            }
        }
    }


    LaunchedEffect(lifecycleOwner) {
        startTracking(context, locationClient, locationRequest, locationCallback)
    }

    DisposableEffect(locationClient) {
        onDispose {
            locationClient.removeLocationUpdates(locationCallback)
        }
    }

    return locationState
}


private suspend fun startTracking(
    context: Context,
    locationClient: FusedLocationProviderClient,
    locationRequest: LocationRequest,
    locationCallback: LocationCallback,
) {
    val hasPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (hasPermission) {
        withContext(Dispatchers.Main) {
            locationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    } else {
        Toast.makeText(context, "Please Enable Location Permissions", Toast.LENGTH_LONG).show()
    }
}

