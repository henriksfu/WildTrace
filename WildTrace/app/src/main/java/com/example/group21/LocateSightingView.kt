package com.example.group21

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.group21.database.Sighting
import com.example.group21.database.SightingViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
fun LocateSightingView(
    sighting: Sighting
) {
    val context = LocalContext.current
    val location = LatLng(sighting.location?.latitude?:0.0, sighting.location?.longitude?:0.0)
    //
    // camera state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 10f)
    }
    //
    // set teh color for the marker border
    val markerBorderColor = MaterialTheme.colorScheme.onSurface
    //
    // the bitmap that will become custom marker
    // set it up.
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(sighting.photoUrl) {
        bitmap = createSightingMarkerBitmap(
            context = context,
            imageUrl = sighting.photoUrl,
            color = markerBorderColor
        )
    }
    //
    // Create the sightingmarker
    val marker: SightingMarker? = remember(bitmap) {
        bitmap?.let {
            SightingMarker(
                state = MarkerState(location),
                thumbnail = BitmapDescriptorFactory.fromBitmap(it),
                sighting = sighting
            )
        }
    }
    var mapLoaded by remember { mutableStateOf(false) }
    val mapProperties = MapProperties(
        isMyLocationEnabled = true
    )

    Box(
        modifier = Modifier.fillMaxSize().statusBarsPadding()
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            onMapLoaded = { mapLoaded = true }
        ) {

            if (marker != null) {
                val rememberedMarkerState =
                    rememberUpdatedMarkerState(position = marker.state.position)
                //
                // Add the custom marker to the map
                Marker(
                    state = rememberedMarkerState,
                    tag = marker.sighting.documentId,
                    title = marker.sighting.animalName,
                    icon = marker.thumbnail,
                    anchor = Offset(0.5f, 1f),
                    visible = marker.isVisible.value,
                )
            }

            LaunchedEffect(mapLoaded) {
                if (mapLoaded) {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(location, 15f),
                        durationMs = 1000
                    )
                }
            }
        }
    }
}