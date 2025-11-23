package com.wildtrace.ui.sightingDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wildtrace.data.ApiRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * ViewModel for handling full sighting detail logic.
 *
 * For Show & Tell 2:
 * - Uses mock data for smooth demo
 * - Has loading state & error state
 * - Ready to plug in real APIs later
 */
class SightingDetailViewModel : ViewModel() {

    // --- UI State ---
    var isLoading by mutableStateOf(true)
    var animalName by mutableStateOf("Loading...")
    var wikiSummary by mutableStateOf("Fetching Wikipedia summary...")
    var imageUrl by mutableStateOf("")           // will be used later when real API gives a photo
    var locationText by mutableStateOf("Loading location…")
    var dateObserved by mutableStateOf("Loading date…")
    var error by mutableStateOf("")

    /**
     * Fetch sighting details.
     * For S&T2:
     * - Shows mock data (API calls optional)
     * - Simulates loading delay for realism
     */
    fun loadSightingDetails(sightingId: String) {
        viewModelScope.launch {
            try {
                isLoading = true
                error = ""

                // Simulate network delay so UI shows a loading spinner
                delay(700)

                animalName = "Bald Eagle"
                wikiSummary = "The bald eagle is a bird of prey found in North America. " +
                        "It is the national bird and symbol of the United States."
                imageUrl = ""  // left blank – UI uses placeholder
                locationText = "Burnaby Mountain, BC"
                dateObserved = "2025-11-21"

                // --- OPTIONAL: Future Real API Calls ---
                // val obsData = ApiRepository.fetchObservationDetails(sightingId)
                // animalName = obsData
                //     ?.optJSONArray("results")
                //     ?.optJSONObject(0)
                //     ?.optString("species_guess") ?: "Unknown"
                //
                // wikiSummary = ApiRepository.fetchWikipediaSummary(animalName)

            } catch (e: Exception) {
                error = "Error loading data: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        println("SightingDetailViewModel cleared.")
    }
}
