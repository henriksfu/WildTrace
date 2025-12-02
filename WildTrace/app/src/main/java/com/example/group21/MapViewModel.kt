package com.example.group21

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.group21.database.Sighting
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerState
import kotlin.random.Random // Import for Random number generation

data class SightingMarker(
    val state: MarkerState,
    val thumbnail: BitmapDescriptor,
    var isVisible: MutableState<Boolean> = mutableStateOf(true),
    val sighting: Sighting
)

class MapViewModel( ) : ViewModel() {
    private val _imageUri = mutableStateOf<Uri?>(null)
    val imageUri: State<Uri?> = _imageUri
    private val _markers = mutableStateListOf<SightingMarker>()
    val markers: List<SightingMarker> get() = _markers
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

    fun setImageUri(uri: Uri, showPhotoDialog: Boolean = true) {
        _imageUri.value = uri
        if(showPhotoDialog) showPhotoDialog()
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
        sightingMarker.value = _markers.find { it.sighting.documentId == sightingID }
        if(sightingMarker.value != null) {
            _showSightingDialog.value = true
        }
    }

    fun dismissSightingDialog() {
        sightingMarker.value = null
        _showSightingDialog.value = false
    }

    fun clearMarkers() {
        _markers.forEach { it.state.hideInfoWindow() }
        _markers.clear()
    }

    fun addMarker(position: LatLng, sighting: Sighting, thumbnail: BitmapDescriptor) {
        val markerState = MarkerState(position = position)

        val newMarker = SightingMarker(
            state = markerState,
            thumbnail = thumbnail,
            sighting = sighting
        )
        _markers.add(newMarker)
    }
    fun toggleMarkers(){
        for (marker in _markers ){
            marker.isVisible.value = !marker.isVisible.value
        }
    }

    /**
     * Finds a random sighting marker location to move the map camera to.
     * @param currentPosition The current map center (used as fallback if no markers exist).
     * @return LatLng of a random marker or the current position.
     */
    fun randomSighting(currentPosition: LatLng): LatLng {
        if (_markers.isEmpty()) {
            return currentPosition
        }
        // Generate a random index between 0 and size - 1
        val randomIndex = Random.nextInt(0, _markers.size)
        return _markers[randomIndex].state.position
    }

}