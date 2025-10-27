package com.wildtrace.ui.sightingDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// This ViewModel will handle fetching the full details of a sighting,
// including the data from the API calls.
class SightingDetailViewModel : ViewModel() {

    // Example function to demonstrate the planned data fetching
    fun loadSightingDetails(sightingId: String) {
        viewModelScope.launch {
            println("SightingDetailViewModel starting to load data for ID: $sightingId")

            // 1. Fetch Sighting data from Firestore
            // println("Fetching Sighting data from Firestore...")

            // 2. Call AI API to confirm/identify the animal
            // println("Calling AI Image Recognition API...")

            // 3. Call Wikipedia API for general information
            // println("Calling Wikipedia API for animal info...")

            println("All data loading logic will be handled here.")
        }
    }

    override fun onCleared() {
        super.onCleared()
        println("SightingDetailViewModel cleared.")
    }
}