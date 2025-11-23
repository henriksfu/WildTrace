package com.example.group21.ui.search.searchView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class SearchViewModel : ViewModel() {

    val searchResults: SnapshotStateList<String> = mutableStateListOf()

    var isLoading: Boolean = false
        private set

    var errorMessage: String? = null
        private set

    /**
     * Called when user presses the Search button.
     * For S&T2, we generate mock results to demonstrate real functionality.
     */
    fun performSearch(query: String) {
        if (query.isBlank()) {
            // If empty search, clear list
            searchResults.clear()
            return
        }

        // Start "loading"
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            // Simulate network delay (makes demo look realistic)
            delay(500)

            // Mock results (replace with real API in final project)
            val dummyResults = listOf(
                "Sighting near Burnaby – $query",
                "Animal: $query (Possible match)",
                "Wikipedia: $query overview",
                "Related Species: ${query}us maximus",
                "More sightings related to \"$query\""
            )

            searchResults.clear()
            searchResults.addAll(dummyResults)

            // Loading finished
            isLoading = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        println("SearchViewModel cleared.")
    }
}
