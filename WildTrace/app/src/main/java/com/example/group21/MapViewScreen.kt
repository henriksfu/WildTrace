package com.example.group21

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.FileProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.group21.database.SightingViewModel
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.MarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

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
    //
    // Default location in case can't find the user's
    val location = userLocation ?: vancouver
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 10f)
    }
    //
    // Observe all sightings
    val allSightings by sightingViewModel.allSightings.observeAsState(emptyList())
    val markerBorderColor = MaterialTheme.colorScheme.onSurface
    //
    // On launch
    LaunchedEffect(allSightings) {
        mapViewModel.clearMarkers()
        //
        if (allSightings.isEmpty()) return@LaunchedEffect
        //
        // Launch all marker loads in parallel so it doesn't take a million years
        val markerJobs = allSightings.map { sighting ->
            async(Dispatchers.IO) {
                val latLng = LatLng(
                    sighting.location?.latitude ?: 0.0,
                    sighting.location?.longitude ?: 0.0
                )
                val bitmap = createSightingMarkerBitmap(
                    context = context,
                    imageUrl = sighting.photoUrl,
                    color = markerBorderColor
                )
                val descriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
                Pair(latLng, sighting to descriptor)
            }
        }
        //
        // Wait for all the bitmap processing stuff
        val results = markerJobs.awaitAll()
        //
        // Add the markers that were generated to the list
        results.forEach { (latLng, pair) ->
            val (sighting, descriptor) = pair
            mapViewModel.addMarker(latLng, sighting, descriptor)
        }
    }
    //
    // get the user's' location and fetch sightings
    LaunchedEffect(Unit){
        mapViewModel.fetchUserLocation(context)
        Log.d("MAP_SCREEN", "Fetching all sightings...")
        sightingViewModel.loadAllSightings()
    }

    var expanded by remember { mutableStateOf(false) }
    var mapLoaded by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme

    val mapProperties = MapProperties(
        isMyLocationEnabled = true
    )
    var clickedPoint by remember {mutableStateOf<LatLng?>(null)}
    var newMarkerBitmap by remember { mutableStateOf<BitmapDescriptor?>(null) }

    Box(modifier = modifier.fillMaxSize().statusBarsPadding()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            onMapClick = { point->
                clickedPoint = point
            } ,
            onMapLongClick =  {
                mapViewModel.toggleMarkers()
            },
            onMapLoaded = {
                mapLoaded = true
            }
        ) {
            mapViewModel.markers.forEach { sightingMarker ->

                Log.e("MAP_MARKERS", "Rendering ${mapViewModel.markers.size} markers")
                //
                // remember the visibility state
                val rememberedMarkerState = rememberUpdatedMarkerState(position = sightingMarker.state.position)
                //
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

            LaunchedEffect(mapLoaded, userLocation) {
                if (mapLoaded && userLocation != null) {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(userLocation!!, 15f),
                        durationMs = 1000
                    )
                }
            }

            if (clickedPoint != null) {
                LaunchedEffect(clickedPoint) {
                    val bitmap = createSightingMarkerBitmap(
                        context = context,
                        imageUrl = "addSighting",
                        color = markerBorderColor
                    )
                    newMarkerBitmap = BitmapDescriptorFactory.fromBitmap(bitmap)
                }
            }

            if (clickedPoint != null) {
                val markerState = rememberUpdatedMarkerState(position = clickedPoint!!)
                markerState.showInfoWindow()
                //
                // remember the visibility state
                val rememberedMarkerState = rememberUpdatedMarkerState(
                    position = LatLng(clickedPoint?.latitude?:0.0, clickedPoint?.longitude?:0.0)
                )
                //
                // Create the plus marker
                Marker(
                    state = rememberedMarkerState,
                    title = "Click the marker to create a new sighting",
                    icon = newMarkerBitmap,
                    anchor = Offset(0.5f, 1f),
                    onClick = {
                        navController.
                        navigate("sighting/${clickedPoint!!.latitude}/${clickedPoint!!.longitude}")
                        true
                    }
                )
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp, 16.dp, 16.dp, 50.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start
        ) {

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                AddSightingButton(
                    navController,
                    mapViewModel,
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    userLocation
                )
                //
                // For Searching entries
                SearchButton(navController)
            }

            // Instruction text below the button
            Text(
                text = "Click the plus or the map to add a sighting",
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
        //
        // When triggered, show the photo preview dialog
        if (mapViewModel.showPhotoDialog.value) {
            PhotoPreviewDialog(
                photoUri = mapViewModel.imageUri.value!!,
                onConfirm = {
                    //
                    // TODO API call to analyze image
                    mapViewModel.dismissPhotoDialog()
                },
                onDismiss = {
                    mapViewModel.dismissPhotoDialog()
                }
            )
        }
    }

    if (mapViewModel.showSightingDialog.value) {
        SightingDisplayDialog(
            sightingMarker = mapViewModel.sightingMarker.value!!,
            onDismiss = {
                mapViewModel.dismissSightingDialog()
            }
        )
    }
}

@Composable
fun SearchButton(navController: NavController){

    val colorScheme = MaterialTheme.colorScheme

    FloatingActionButton(
        onClick = {
            navController.navigate("search")
        },
        containerColor = colorScheme.background,
        contentColor = colorScheme.onBackground,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .size(width = 90.dp, height = 90.dp)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = "Search for Sightings",
            modifier = Modifier.fillMaxSize(0.5f)
        )
    }
}
@Composable
//
// This button hides two smaller popouts that let the user select "manual" entry or "automatic" entry
fun AddSightingButton(
    navController: NavController,
    mapViewModel: MapViewModel,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    userLocation: LatLng?,
){
    val colorScheme = MaterialTheme.colorScheme
    //
    Box(
        modifier = Modifier.wrapContentSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        FloatingActionButton(
            onClick = {
                onExpandedChange(false)
                val lat = userLocation?.latitude ?: vancouver.latitude
                val lng = userLocation?.longitude ?: vancouver.longitude
                navController.navigate("sighting/${lat}/${lng}/${""}/${""}/${-1L}/${-1L}/${""}")
            },
            containerColor = colorScheme.background,
            contentColor = colorScheme.onBackground,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .size(width = 90.dp, height = 90.dp)
                .padding(8.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Manual entry",
                modifier = Modifier.fillMaxSize(0.5f)
            )
        }
    }
}

fun createImageFile(context: Context): Uri {

    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CANADA).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"

    val storageDir = context.externalCacheDir
    val image = File.createTempFile(
        imageFileName,
        ".jpg",
        storageDir
    )

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        image
    )
}