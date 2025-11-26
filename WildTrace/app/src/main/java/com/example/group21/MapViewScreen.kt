package com.example.group21

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.ui.unit.sp

@Composable
fun MapViewScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val userLocation = getUserLocation().value
    val vancouver = LatLng(49.2827, -123.1207)
    val location = userLocation ?: vancouver
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 10f)
    }
    val scope = rememberCoroutineScope()

    var expanded by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme
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
            onConfirm = {
                mapViewModel.dismissSightingDialog()
            },
            onDismiss = {
                mapViewModel.dismissSightingDialog()
            },
            sighting = mapViewModel.sightingMarker.value!!
        )
    }



    val mapProperties = MapProperties(
        isMyLocationEnabled = true
    )
    var clickedPoint by remember {mutableStateOf<LatLng?>(null)}
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
            }

        ) {
            mapViewModel.markers.forEach { sightingMarker ->
                Marker(
                    tag = sightingMarker.id,
                    state = sightingMarker.state,
                    title = sightingMarker.title + " Sighting",
                    snippet = "Click this to see full details",
                    visible = sightingMarker.isVisible.value,
                    onClick = {
                        sightingMarker.state.showInfoWindow()
                        true
                    },
                    onInfoWindowClick = {
                        mapViewModel.showSightingDialog(it.tag as String)
                    }

                )
            }

            if (clickedPoint != null) {
                val markerState = rememberUpdatedMarkerState(position = clickedPoint!!)
                markerState.showInfoWindow()
                Marker(
                    state = markerState,
                    title = "Click the marker to create a new sighting",
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
                    onExpandedChange = { expanded = it }
                )

                // For Searching entries
                FloatingActionButton(
                    onClick = { /* TODO */ },
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

            // Instruction text below the button
            Text(
                text = "Upload a picture or take a new one",
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
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 100.dp
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            val maxWidth = 180.dp
            Button(onClick = {
                navController.
                navigate("sighting/${cameraPositionState.position.target.latitude}/${cameraPositionState.position.target.longitude}")
            },
                modifier = Modifier.widthIn(max = maxWidth))
            {
                Text("Create New Sighting at Current Location",
                    maxLines = Int.MAX_VALUE,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Button(onClick = {
                scope.launch {
                    val currentPosition = cameraPositionState.position.target
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLng(
                            mapViewModel.randomSighting(
                                currentPosition
                            )
                        )
                    )
                }
            },
                modifier = Modifier.widthIn(max = maxWidth))
            {
                Text("Go to Random Sighting",
                    maxLines = Int.MAX_VALUE,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun AddSightingButton(
    navController: NavController,
    mapViewModel: MapViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
){
    //
    // Is it expanded
    val colorScheme = MaterialTheme.colorScheme

    var uri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                mapViewModel.setImageUri(uri!!)
            }
        }
    )

    if (mapViewModel.showPhotoDialog.value) {
        PhotoPreviewDialog(
            photoUri = mapViewModel.imageUri.value!!,
            onConfirm = {
                mapViewModel.dismissPhotoDialog()
                navController.navigate("sightingDetail")
            },
            onDismiss = {
                mapViewModel.dismissPhotoDialog()
            }
        )
    }


    Box(
        modifier = Modifier.wrapContentSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn()  + slideInHorizontally  { -it / 2 },
            exit  = fadeOut() + slideOutHorizontally { -it / 2 }
        ) {
            Column() {

                FloatingActionButton(
                    onClick = {
                        onExpandedChange(false)
                        uri = createImageFile(context)
                        cameraLauncher.launch(uri!!)
                    },
                    containerColor = colorScheme.background,
                    contentColor = colorScheme.onBackground,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .size(width = 90.dp, height = 90.dp)
                        .padding(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = "Take a Photo",
                        modifier = Modifier.fillMaxSize(0.5f)
                    )
                }

                FloatingActionButton(
                    onClick = {
                        onExpandedChange(false)
                    },
                    containerColor = colorScheme.background,
                    contentColor = colorScheme.onBackground,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .size(width = 90.dp, height = 90.dp)
                        .padding(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.FileUpload,
                        contentDescription = "Upload A File",
                        modifier = Modifier.fillMaxSize(0.5f)
                    )
                }

            }
        }

        AnimatedVisibility(
            visible = !expanded,
            enter = fadeIn()  + slideInHorizontally  { -it / 2 },
            exit  = fadeOut() + slideOutHorizontally { -it / 2 }
        ) {
            FloatingActionButton(
                onClick = {
                    onExpandedChange(true)
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
                    contentDescription = "Search",
                    modifier = Modifier.fillMaxSize(0.5f)
                )
            }
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