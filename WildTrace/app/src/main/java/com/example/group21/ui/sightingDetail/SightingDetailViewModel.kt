package com.wildtrace.ui.sightingDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class SightingDetailViewModel : ViewModel() {

    // --- UI State ---
    var isLoading by mutableStateOf(true)
    var animalName by mutableStateOf("Loading...")
    var wikiSummary by mutableStateOf("Fetching Wikipedia summary...")
    var imageUrl by mutableStateOf("")
    var locationText by mutableStateOf("Loading location…")
    var dateObserved by mutableStateOf("Loading date…")
    var error by mutableStateOf("")

    /**
     * Fetch sighting details.
     * S&T 2 Status:
     * - Sighting Data: MOCK (Simulates Firestore)
     * - Wikipedia Data: REAL (Live API call)
     */
    fun loadSightingDetails(sightingId: String) {
        viewModelScope.launch {
            try {
                isLoading = true
                error = ""

                // 1. Simulate fetching basic sighting info from Firestore (Mock for now)
                // In the final project, this will come from Person 1's Firestore collection
                animalName = "Bald Eagle"
                locationText = "Burnaby Mountain, BC"
                dateObserved = "2025-11-23"

                // 2. REAL API CALL: Fetch summary from Wikipedia
                // We offload this to the IO thread to keep the UI smooth
                val summary = fetchWikipediaSummary(animalName)
                wikiSummary = summary

            } catch (e: Exception) {
                error = "Error loading data: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Hits the Wikipedia REST API to get a summary.
     * Runs on Dispatchers.IO (Background Thread).
     */
    private suspend fun fetchWikipediaSummary(query: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // Format the query (Wikipedia expects underscores instead of spaces)
                val formattedQuery = query.trim().replace(" ", "_")
                val urlString = "https://en.wikipedia.org/api/rest_v1/page/summary/$formattedQuery"

                // Open connection
                val url = URL(urlString)
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.connect()

                if (connection.responseCode == 200) {
                    // Read the response
                    val stream = connection.inputStream
                    val responseText = stream.bufferedReader().use { it.readText() }

                    // Parse JSON (Standard Android JSON library)
                    val json = JSONObject(responseText)

                    // "extract" contains the short summary
                    json.optString("extract", "No description available.")
                } else {
                    "Wikipedia article not found (Code: ${connection.responseCode})"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                "Failed to connect to Wikipedia. Check your internet."
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        println("SightingDetailViewModel cleared.")
    }
}