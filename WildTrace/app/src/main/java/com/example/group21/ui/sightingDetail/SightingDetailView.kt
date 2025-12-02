package com.example.group21.ui.search.sightingDetail

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.group21.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SightingDetailView(
    navController: NavController,
    capturedImage: Bitmap?,
    capturedUri: Uri?,
    onBack: () -> Unit = {},
    viewModel: SightingDetailViewModel = viewModel(),
    latitude: Float?,
    longitude: Float?,
    animalName: String,
    comment: String,
    date: Long,
    time: Long

) {
    //
    // to make scrollable
    val scrollState = rememberScrollState()
    //
    val context = LocalContext.current
    //
    // asynchronously build the image
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(capturedUri)
        .error(com.example.group21.R.drawable.image_not_found)
        .fallback(R.drawable.image_not_found)
        .build()
    //
    // When screen opens, send the photo to the ViewModel for AI Analysis
    LaunchedEffect(Unit) {
        if (capturedImage != null) {
            viewModel.onConfirmAndSearch(context, capturedImage)
        } else {
            // Handle case where we are viewing an old sighting (no new photo)
            viewModel.onConfirmAndSearch(context, null)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    text = "Sighting Details",
                    color = MaterialTheme.colorScheme.onBackground
                ) },
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.dp, bottom = 48.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        navController.navigate("sighting/${latitude}/${longitude}/${animalName}/${comment}/${date}/${time}/${""}")
                    },
                    modifier = Modifier
                        .padding(8.dp),
                    enabled = !viewModel.isLoading
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onBackground)
                }
                Button(
                    onClick = {
                        navController.navigate("sighting/${latitude}/${longitude}/${animalName}/${comment}/${date}/${time}/${""}")
                    },
                    modifier = Modifier
                        .padding(8.dp),
                    enabled = !viewModel.isLoading
                ) {
                    if (viewModel.isLoading) {
                        Text("Analyzing...", color = MaterialTheme.colorScheme.onBackground)
                    } else {
                        Text("Save Sighting", color = MaterialTheme.colorScheme.onBackground)
                    }
                }

            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (capturedImage != null) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = "Sighting Photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                        .padding(bottom = 16.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                // Fallback to Wikipedia Image (e.g. if viewing history)
                AnimalImage(url = viewModel.imageUrl)
            }

            // --- Loading & Results Section ---
            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Identifying Animal...",
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 48.dp)
                    )
                }
            } else {
                if (viewModel.error.isNotEmpty()) {
                    Text(
                        text = viewModel.error,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    // Only show data if we have loaded something
                    if (viewModel.animalName != "Loading...") {

                        // 1. Name
                        Text(
                            text = "Identified: ${viewModel.animalName}",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        // 2. Metadata (Location/Date)
                        MetadataSection(
                            location = viewModel.locationText,
                            date = viewModel.dateObserved
                        )

                        HorizontalDivider()

                        // 3. Wikipedia Summary
                        Text(
                            text = "About this animal:",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = viewModel.wikiSummary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

// --- Helpers ---

@Composable
fun AnimalImage(url: String) {
    if (url.isNotEmpty()) {
        AsyncImage(
            model = url,
            contentDescription = "Wiki Animal Photo",
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "📍 ", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = location,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "📅 ", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = date,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}