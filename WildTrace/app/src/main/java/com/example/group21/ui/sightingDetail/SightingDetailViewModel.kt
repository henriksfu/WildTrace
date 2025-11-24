package com.example.group21.ui.search.sightingDetail

import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class SightingDetailViewModel : ViewModel() {

    // --- UI State ---
    var isLoading by mutableStateOf(false)
    var animalName by mutableStateOf("Loading...")
    var wikiSummary by mutableStateOf("")
    var imageUrl by mutableStateOf("")
    var locationText by mutableStateOf("Locating...")
    var dateObserved by mutableStateOf("Loading date…")
    var error by mutableStateOf("")

    /**
     * Called when the user clicks "Confirm & Search" (or auto-loads).
     * Now requires Context to fetch Real Location.
     */
    fun onConfirmAndSearch(context: Context) {
        viewModelScope.launch {
            try {
                isLoading = true
                error = ""

                // 1. Get REAL Date & Time
                val sdf = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
                dateObserved = sdf.format(Date())

                // 2. Get REAL Location (City, Country)
                // We run this in background to avoid freezing UI
                fetchRealLocation(context)

                // 3. Simulate finding a Hippo (iNat Mock)
                animalName = "Hippopotamus"

                // 4. Fetch Wikipedia Data
                val result = fetchWikipediaData(animalName)
                wikiSummary = result.first
                imageUrl = result.second

            } catch (e: Exception) {
                error = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun fetchRealLocation(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

                // Check permissions (Basic check since MapView already requested them)
                try {
                    // Try GPS first, then Network
                    val lastLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                    if (lastLoc != null) {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        // Get address from coordinates
                        val addresses = geocoder.getFromLocation(lastLoc.latitude, lastLoc.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0]
                            // Format: "Burnaby, Canada"
                            val city = address.locality ?: address.subAdminArea ?: "Unknown City"
                            val country = address.countryName ?: ""
                            locationText = "$city, $country"
                        } else {
                            locationText = "Lat: ${lastLoc.latitude}, Lng: ${lastLoc.longitude}"
                        }
                    } else {
                        locationText = "Location not found"
                    }
                } catch (e: SecurityException) {
                    locationText = "Permission denied"
                }
            } catch (e: Exception) {
                locationText = "Unknown Location"
            }
        }
    }

    private suspend fun fetchWikipediaData(query: String): Pair<String, String> {
        return withContext(Dispatchers.IO) {
            try {
                val formattedQuery = query.trim().replace(" ", "_")
                val urlString = "https://en.wikipedia.org/api/rest_v1/page/summary/$formattedQuery"

                val url = URL(urlString)
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "WildTraceStudentApp/1.0 (student@university.edu)")
                connection.connect()

                if (connection.responseCode == 200) {
                    val stream = connection.inputStream
                    val responseText = stream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseText)

                    val extract = json.optString("extract", "No description available.")
                    val thumbnailJson = json.optJSONObject("thumbnail")
                    val imageSource = thumbnailJson?.optString("source") ?: ""

                    Pair(extract, imageSource)
                } else {
                    Pair("Wikipedia article not found.", "")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Pair("Failed to connect.", "")
            }
        }
    }
}