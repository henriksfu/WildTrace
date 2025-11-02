package com.example.group21.ui.search.searchView

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

// This composable represents the Search screen of the app.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchView(
    // The ViewModel is injected here, using viewModel() to create/get the ViewModel
    // from the lifecycle scope.
    viewModel: SearchViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Search") })
        }
    ) { paddingValues ->
        // Use Box for simple centering of content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder text to demonstrate the screen is loaded.
            Text(text = "Search functionality coming soon...")
        }
    }
}