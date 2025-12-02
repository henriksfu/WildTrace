package com.example.group21

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.group21.database.SightingViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MapViewScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel
) {
    val userLocation = getUserLocation().value
    val vancouver = LatLng(49.2827, -123.1207)
    val location = userLocation ?: vancouver
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 10f)
    }
    val scope = rememberCoroutineScope()

    val sightingViewModel: SightingViewModel = viewModel()

    LaunchedEffect(Unit) {
        sightingViewModel.loadAllSightings()
    }

    LaunchedEffect(userLocation) {
        userLocation?.let {
            if (cameraPositionState.position.target != it) {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(it, 15f),
                    durationMs = 1000
                )
            }
        }
    }

    if (mapViewModel.showSightingDialog.value) {
        SightingDisplayDialog(
            onConfirm = { mapViewModel.dismissSightingDialog() },
            onDismiss = { mapViewModel.dismissSightingDialog() },
            sighting = mapViewModel.sightingMarker.value!!
        )
    }

    val mapProperties = MapProperties(isMyLocationEnabled = true)
    var clickedPoint by remember { mutableStateOf<LatLng?>(null) }

    Box(modifier = modifier.fillMaxSize().statusBarsPadding()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            onMapClick = { point -> clickedPoint = point },
            onMapLongClick = { mapViewModel.toggleMarkers() }
        ) {
            mapViewModel.markers.forEach { sightingMarker ->
                Marker(
                    tag = sightingMarker.id,
                    state = sightingMarker.state,
                    title = sightingMarker.title + " Sighting",
                    snippet = "Click to see details",
                    visible = sightingMarker.isVisible.value,
                    onClick = {
                        sightingMarker.state.showInfoWindow()
                        true
                    },
                    onInfoWindowClick = { mapViewModel.showSightingDialog(it.tag as String) }
                )
            }

            if (clickedPoint != null) {
                val markerState = rememberUpdatedMarkerState(position = clickedPoint!!)
                Marker(
                    state = markerState,
                    title = "New Sighting Here?",
                    snippet = "Tap + button to add",
                    onClick = {
                        markerState.showInfoWindow()
                        true
                    }
                )
            }
        }

        // --- BOTTOM LEFT CONTROLS (Expandable + Button) ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp, 16.dp, 16.dp, 50.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.Bottom) {

                // ✅ THE NEW EXPANDABLE BUTTON LOGIC
                ExpandableSightingButton(
                    navController = navController,
                    mapViewModel = mapViewModel,
                    currentCameraTarget = cameraPositionState.position.target
                )

                // Search Button
                FloatingActionButton(
                    onClick = { navController.navigate("search") },
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.size(90.dp).padding(8.dp)
                ) {
                    Icon(Icons.Filled.Search, "Search", Modifier.fillMaxSize(0.5f))
                }
            }
        }

        // --- BOTTOM RIGHT CONTROLS ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp, 16.dp, 16.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            val maxWidth = 180.dp
            Button(onClick = { sightingViewModel.loadAllSightings() }, modifier = Modifier.widthIn(max = maxWidth)) {
                Text("Refresh Sightings", textAlign = TextAlign.Center)
            }
            Button(onClick = {
                scope.launch {
                    try {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLng(
                                mapViewModel.randomSighting(cameraPositionState.position.target)
                            )
                        )
                    } catch (e: Exception) { Log.e("Map", "Error navigating") }
                }
            }, modifier = Modifier.widthIn(max = maxWidth)) {
                Text("Random Sighting", textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun ExpandableSightingButton(
    navController: NavController,
    mapViewModel: MapViewModel,
    currentCameraTarget: LatLng
) {
    var expanded by remember { mutableStateOf(false) }
    var tempUri by remember { mutableStateOf(Uri.EMPTY) }
    val context = LocalContext.current

    // 1. Camera Logic
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                // Save to ViewModel so Next Screen sees it
                mapViewModel.setImageUri(tempUri)
                // Navigate
                navController.navigate("sighting/${currentCameraTarget.latitude}/${currentCameraTarget.longitude}")
                expanded = false
            }
        }
    )

    // 2. Gallery Logic
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                mapViewModel.setImageUri(uri)
                navController.navigate("sighting/${currentCameraTarget.latitude}/${currentCameraTarget.longitude}")
                expanded = false
            }
        }
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        // --- Mini Buttons (Visible when Expanded) ---
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 12.dp)) {

                // GALLERY BUTTON
                FloatingActionButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(Icons.Filled.PhotoLibrary, "Gallery")
                }

                // CAMERA BUTTON
                FloatingActionButton(
                    onClick = {
                        tempUri = createImageFileForMap(context)
                        cameraLauncher.launch(tempUri)
                    },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(Icons.Filled.CameraAlt, "Camera")
                }
            }
        }

        // --- Main Toggle Button (+ / X) ---
        FloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.size(90.dp).padding(8.dp)
        ) {
            Icon(
                imageVector = if (expanded) Icons.Filled.Close else Icons.Filled.Add,
                contentDescription = "Add",
                modifier = Modifier.fillMaxSize(0.5f)
            )
        }
    }
}

// Private helper for this file
private fun createImageFileForMap(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(null)
    val image = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", image)
}