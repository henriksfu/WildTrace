package com.example.group21

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerState
import kotlin.math.abs

data class SightingMarker(
    val id: String,
    val state: MarkerState,
    val title: String,
    var isVisible: MutableState<Boolean> = mutableStateOf(true),
    val comment: String,
    val imageUri: Uri?
)

class MapViewModel( ) : ViewModel() {
    private val _imageUri = mutableStateOf<Uri?>(null)
    val imageUri: State<Uri?> = _imageUri
    private val _markers = mutableStateListOf<SightingMarker>()
    val markers: List<SightingMarker> = _markers
    private val _showSightingDialog = mutableStateOf(false)
    val showSightingDialog: State<Boolean> = _showSightingDialog
    val sightingMarker: MutableState<SightingMarker?> = mutableStateOf(null)
    private val _userLocation = mutableStateOf<LatLng?>(null)
    val userLocation: State<LatLng?> = _userLocation
    private lateinit var fusedClient: FusedLocationProviderClient

    //
    // Get the user' current location. This is used for adding entries
    @SuppressLint("MissingPermission")
    fun fetchUserLocation(context: Context) {
        //
        // If the fused client hasn't been created yet, do so with the context passed
        if( !::fusedClient.isInitialized ) {
            fusedClient = LocationServices.getFusedLocationProviderClient(context)
        }
        //
        // Use the client to get the current location
        fusedClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->
            if (location != null) {
                _userLocation.value = LatLng(location.latitude, location.longitude)
            }
        }
    }

    fun setImageUri(uri: Uri) {
        _imageUri.value = uri
        showPhotoDialog()
    }

    init {
       addMarker(
            LatLng(49.222032865117825, -123.0723162798196),
            "Dog"
        )
        addMarker(
            LatLng(49.24511450409677, -123.05986998151903),
            "Cat"
        )
        addMarker(
            LatLng(49.23650974676318, -123.01989096273527),
            "Bird"
        )
        addMarker(
            LatLng(49.224530359744705, -123.00895330665291),
            "Bear"
        )
        addMarker(
            LatLng(49.214436959993215, -123.01259919201368),
            "Raccoon"
        )
    }

    private val _showPhotoDialog = mutableStateOf(false)
    val showPhotoDialog: State<Boolean> = _showPhotoDialog

    fun showPhotoDialog(){
        _showPhotoDialog.value = true
    }

    fun dismissPhotoDialog() {
        _showPhotoDialog.value = false
    }

    fun showSightingDialog(sightingID: String){
        sightingMarker.value = _markers.find { it.id == sightingID }
        _showSightingDialog.value = true
    }

    fun dismissSightingDialog() {
        sightingMarker.value = null
        _showSightingDialog.value = false
    }


    private fun addMarker(position: LatLng, title: String, imageUri: Uri? = null, comment: String = "") {
        val markerState = MarkerState(position = position)
        markerState.showInfoWindow()

        val newMarker = SightingMarker(
            id = java.util.UUID.randomUUID().toString(),
            state = markerState,
            title = title,
            comment = comment,
            imageUri =imageUri
        )
        _markers.add(newMarker)
    }
    fun toggleMarkers(){
        for (marker in _markers){
            marker.isVisible.value = !marker.isVisible.value
        }
    }
    fun randomSighting(currentPosition: LatLng): LatLng {
        if (markers.size <= 1){
            return currentPosition
        }
        var marker = _markers.random()
        val maxIters = 10
        var iters = 0
        val tol = 0.00001
        while (abs(marker.state.position.latitude - currentPosition.latitude) < tol
            && abs(marker.state.position.longitude - currentPosition.longitude) < tol
            && iters < maxIters ){
                marker = _markers.random()
                iters++
        }
        marker.state.showInfoWindow()
        return marker.state.position
    }

}

