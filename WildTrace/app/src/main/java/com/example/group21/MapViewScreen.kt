package com.example.group21

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.ui.graphics.toArgb


val vancouver = LatLng(49.2827, -123.1207)

@Composable
fun MapViewScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel,
    sightingViewModel: SightingViewModel
) {
    val context = LocalContext.current
    val userLocation by mapViewModel.userLocation
    val location = userLocation ?: vancouver
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 10f)
    }
    val scope = rememberCoroutineScope()

    // --- Remote Sighting Loading Logic ---
    val allSightings by sightingViewModel.allSightings.observeAsState(emptyList())
    // markerBorderColor is a Compose Color object
    val markerBorderColor = MaterialTheme.colorScheme.onSurface
    var mapLoaded by remember { mutableStateOf(false) }
    var clickedPoint by remember { mutableStateOf<LatLng?>(null) }
    var newMarkerBitmap by remember { mutableStateOf<BitmapDescriptor?>(null) }

    val colorScheme = MaterialTheme.colorScheme // Access here for styling

    // Load and create markers when new sightings arrive
    LaunchedEffect(allSightings) {
        mapViewModel.clearMarkers()
        if (allSightings.isEmpty()) return@LaunchedEffect
        for (sighting in allSightings) {
            // FIX: Pass Compose Color (markerBorderColor) to the stub helper
            val bitmap = createSightingMarkerBitmap(context, sighting.photoUrl, markerBorderColor)
            val descriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
            val lat = sighting.location?.latitude ?: 0.0
            val lng = sighting.location?.longitude ?: 0.0
            val latLng = LatLng(lat, lng)
            mapViewModel.addMarker(latLng, sighting, descriptor)
        }
    }

    // Initial load and movement
    LaunchedEffect(Unit) {
        mapViewModel.fetchUserLocation(context)
        sightingViewModel.loadAllSightings()
    }

    // Animate camera when user location is found
    LaunchedEffect(mapLoaded, userLocation) {
        if (mapLoaded && userLocation != null) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(userLocation!!, 15f),
                durationMs = 1000
            )
        }
    }
    // --- End Remote Logic ---


    // Dialog for displaying sighting details
    if (mapViewModel.showSightingDialog.value) {
        SightingDisplayDialog( // FIX: Calls stub SightingDisplayDialog
            sightingMarker = mapViewModel.sightingMarker.value!!,
            onDismiss = { mapViewModel.dismissSightingDialog() }
        )
    }

    val mapProperties = MapProperties(isMyLocationEnabled = true)

    Box(modifier = modifier.fillMaxSize().statusBarsPadding()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            onMapClick = { point -> clickedPoint = point },
            onMapLongClick = { mapViewModel.toggleMarkers() },
            onMapLoaded = { mapLoaded = true }
        ) {
            // Render all remote markers
            mapViewModel.markers.forEach { sightingMarker ->
                val rememberedMarkerState = rememberUpdatedMarkerState(position = sightingMarker.state.position)
                Marker(
                    tag = sightingMarker.sighting.documentId,
                    state = rememberedMarkerState,
                    title = sightingMarker.sighting.animalName,
                    icon = sightingMarker.thumbnail,
                    anchor = Offset(0.5f, 1f),
                    snippet = "Click to see more details",
                    visible = sightingMarker.isVisible.value,
                    onClick = {
                        mapViewModel.showSightingDialog(it.tag as String)
                        true
                    }
                )
            }

            // Render the temporary marker on map click
            if (clickedPoint != null) {
                LaunchedEffect(clickedPoint) {
                    val bitmap = createSightingMarkerBitmap(
                        context = context,
                        imageUrl = "addSighting",
                        color = markerBorderColor // Compose Color
                    )
                    newMarkerBitmap = BitmapDescriptorFactory.fromBitmap(bitmap)
                }
                val markerState = rememberUpdatedMarkerState(position = clickedPoint!!)
                markerState.showInfoWindow()
                Marker(
                    state = markerState,
                    title = "Click the marker to create a new sighting",
                    icon = newMarkerBitmap,
                    anchor = Offset(0.5f, 1f),
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

                // ✅ EXPANDABLE BUTTON
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

            // Instruction text below the button
            Text(
                text = "Tap + or map to add a sighting",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
                color = colorScheme.onBackground,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .background(
                        color = colorScheme.background,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(8.dp)
            )
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
                        // FIX: Assuming mapViewModel now correctly contains randomSighting
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

// --- EXPANDABLE PLUS BUTTON LOGIC ---

@Composable
fun ExpandableSightingButton(
    navController: NavController,
    mapViewModel: MapViewModel,
    currentCameraTarget: LatLng
) {
    var expanded by remember { mutableStateOf(false) }
    var tempUri by remember { mutableStateOf(Uri.EMPTY) }
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    // 1. Camera Logic
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                // Handoff to NewSightingEntry
                mapViewModel.setImageUri(tempUri)
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
                // Handoff to NewSightingEntry
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

                // 🖼️ GALLERY BUTTON (Automatic Entry)
                FloatingActionButton(
                    onClick = {
                        galleryLauncher.launch("image/*")
                    },
                    containerColor = colorScheme.tertiaryContainer,
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(Icons.Filled.PhotoLibrary, "Gallery")
                }

                // 🖊️ MANUAL ENTRY BUTTON
                FloatingActionButton(
                    onClick = {
                        // Navigate directly to the entry form (Manual Entry)
                        navController.navigate("sighting/${currentCameraTarget.latitude}/${currentCameraTarget.longitude}")
                        expanded = false
                    },
                    containerColor = colorScheme.tertiaryContainer,
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(Icons.Filled.Draw, "Manual Entry")
                }
            }
        }

        // --- Main Toggle Button (+ / X) ---
        FloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = colorScheme.primary,
            contentColor = colorScheme.onPrimary,
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

// --- STUB: Missing function definition (Needed for Marker logic) ---
// This function needs to be defined outside of MapViewScreen.
fun createSightingMarkerBitmap(
    context: Context,
    imageUrl: String,
    color: Color
): android.graphics.Bitmap {
    // Placeholder implementation (Returns a blank bitmap for compilation)
    return android.graphics.Bitmap.createBitmap(96, 96, android.graphics.Bitmap.Config.ARGB_8888)
}

// --- Helper Function for Camera (Kept separate from Composable) ---
private fun createImageFileForMap(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(null)
    val image = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", image)
}