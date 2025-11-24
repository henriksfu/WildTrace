package com.example.group21.ui.search.sightingDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SightingDetailView(
    sightingId: String = "10001",
    onBack: () -> Unit = {},
    viewModel: SightingDetailViewModel = viewModel()
) {
    val context = LocalContext.current

    // ✅ Pass Context to get Real Location/Date on entry
    LaunchedEffect(Unit) {
        viewModel.onConfirmAndSearch(context)
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
        },
        bottomBar = {
            Button(
                onClick = {
                    // ✅ Pass Context here too
                    viewModel.onConfirmAndSearch(context)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = !viewModel.isLoading
            ) {
                if (viewModel.isLoading) {
                    Text("Searching...")
                } else {
                    Text("Save the Sighting")
                }
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            AnimalImage(url = viewModel.imageUrl)

            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                if (viewModel.error.isNotEmpty()) {
                    Text(
                        text = viewModel.error,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    if (viewModel.animalName != "Loading...") {
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
        }
    }
}

@Composable
fun AnimalImage(url: String) {
    if (url.isNotEmpty()) {
        AsyncImage(
            model = url,
            contentDescription = "Animal Photo",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Gray),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.LightGray, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("No Image Available", style = MaterialTheme.typography.bodyMedium)
        }
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