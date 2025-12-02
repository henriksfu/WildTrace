package com.example.group21.ui.search.sightingDetail

import android.content.Context
import android.graphics.Bitmap
import android.location.Geocoder
import android.location.LocationManager
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
     * MAIN FUNCTION: Called when user confirms the photo.
     */
    fun onConfirmAndSearch(context: Context, imageBitmap: Bitmap?) {
        viewModelScope.launch {
            try {
                isLoading = true
                error = ""

                // 1. Get REAL Date & Time
                val sdf = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
                dateObserved = sdf.format(Date())

                // 2. Get REAL Location (City, Country)
                fetchRealLocation(context)

                // 3. AI Identification (NOW USING GOOGLE CLOUD VISION API)
                if (imageBitmap != null) {
                    animalName = "Identifying..."
                    val detectedName = identifyAnimalWithGoogleVision(imageBitmap)
                    animalName = detectedName
                } else {
                    animalName = "No Image Found"
                }

                // 4. Fetch Wikipedia Data (using the name found by API)
                val result = fetchWikipediaData(animalName)
                wikiSummary = result.first
                imageUrl = result.second

            } catch (e: Exception) {
                error = "Error: ${e.message}"
                Log.e("SightingViewModel", "Error in search flow", e)
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * ✅ FINAL API: GOOGLE CLOUD VISION API IMPLEMENTATION
     * Connects to the same highly accurate model used on the Google website demo.
     */
    // In SightingDetailViewModel.kt, REPLACE the identifyAnimalWithGoogleVision function:

    private suspend fun identifyAnimalWithGoogleVision(bitmap: Bitmap): String {
        return withContext(Dispatchers.IO) {

            // ✅ CORRECTED: Read key securely from BuildConfig.GC_VISION_KEY
            val GC_VISION_KEY = com.example.group21.BuildConfig.GC_VISION_KEY

            try {
                // 1. Resize and Convert to Base64 (Standard procedure)
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 800, 600, true)
                val stream = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                val base64Image = Base64.encodeToString(
                    stream.toByteArray(),
                    Base64.NO_WRAP or Base64.URL_SAFE
                )

                // 2. Construct JSON Body for Google Vision API
                val jsonBody = """
            {
              "requests": [
                {
                  "image": {
                    "content": "$base64Image"
                  },
                  "features": [
                    {
                      "type": "LABEL_DETECTION",
                      "maxResults": 10
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

                // 3. Send Network Request to Google Endpoint
                // Key is included as a query parameter
                val urlString =
                    "https://vision.googleapis.com/v1/images:annotate?key=$GC_VISION_KEY"
                val url = URL(urlString)
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                connection.outputStream.use { it.write(jsonBody.toByteArray()) }

                val responseCode = connection.responseCode

                if (responseCode == 200) {
                    // Success logic
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)

                    // Navigate Google's JSON path: responses -> [0] -> labelAnnotations
                    val labelAnnotations = json
                        .getJSONArray("responses")
                        .getJSONObject(0)
                        .getJSONArray("labelAnnotations")

                    if (labelAnnotations.length() > 0) {
                        // Return the top result. Google's model is strong.
                        return@withContext labelAnnotations
                            .getJSONObject(0)
                            .getString("description")
                            .replaceFirstChar { it.uppercase() } // E.g., "tiger" -> "Tiger"
                    }
                    return@withContext "Unknown Animal"
                } else {
                    // Failure logic: Read the error stream for diagnostics
                    val errorStream = connection.errorStream ?: connection.inputStream
                    val errorResponse = errorStream.bufferedReader().use { it.readText() }
                    Log.e("GC_VISION_ERROR", "Status: $responseCode. Response: $errorResponse")

                    return@withContext "API Error: $responseCode - Check Logcat"
                }
            } catch (e: Exception) {
                Log.e("GC_VISION_ERROR", "Exception during API call: ${e.message}", e)
                return@withContext "Connection Error: ${e.message}"
            }
        }
    }

    // --- Existing Helper: Location ---
    private suspend fun fetchRealLocation(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

                try {
                    val lastLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                    if (lastLoc != null) {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(lastLoc.latitude, lastLoc.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0]
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

    // --- Existing Helper: Wikipedia ---
    private suspend fun fetchWikipediaData(query: String): Pair<String, String> {
        return withContext(Dispatchers.IO) {
            try {
                val formattedQuery = query.trim().replace(" ", "_")
                val urlString = "https://en.wikipedia.org/api/rest_v1/page/summary/$formattedQuery"

                val url = URL(urlString)
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "WildTraceStudentApp/1.0")
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