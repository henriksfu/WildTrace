package com.example.group21.ui.search.searchView

import androidx.lifecycle.ViewModel

// The ViewModel handles the business logic and state for the SearchView.
class SearchViewModel : ViewModel() {
    // We can initialize any state here, like the search query or results list.
    // For now, it's just a simple class to demonstrate the architecture.

    // Example of a function that would be implemented later:
    fun performSearch(query: String) {
        // Log or call a repository to fetch data
        println("Searching for: $query")
    }

    override fun onCleared() {
        super.onCleared()
        // Called when the ViewModel is no longer used, useful for cleanup.
        println("SearchViewModel cleared.")
    }
}