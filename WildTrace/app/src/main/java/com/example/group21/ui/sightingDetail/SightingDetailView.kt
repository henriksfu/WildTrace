package com.wildtrace.ui.sightingDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SightingDetailView(
    sightingId: String = "10001",
    onBack: () -> Unit = {},
    viewModel: SightingDetailViewModel = viewModel()
) {
    // Load data once on entering detail screen
    LaunchedEffect(Unit) {
        viewModel.loadSightingDetails(sightingId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sighting Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            if (viewModel.error.isNotEmpty()) {
                Text(
                    text = viewModel.error,
                    color = MaterialTheme.colorScheme.error
                )
                return@Column
            }

            PhotoPlaceholder()

            Text(
                text = "Animal Identified: ${viewModel.animalName}",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = viewModel.wikiSummary,
                style = MaterialTheme.typography.bodyMedium
            )

            MetadataSection(
                location = viewModel.locationText,
                date = viewModel.dateObserved
            )
        }
    }
}

@Composable
fun PhotoPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.LightGray, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("Photo Placeholder", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun MetadataSection(location: String, date: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Observed At: $location",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "Date: $date",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
