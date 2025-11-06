package com.example.group21.ui.search.sightingDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.material3.Button
import androidx.compose.foundation.layout.fillMaxWidth

import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SightingDetailView(
    // 'sightingId' would be passed through navigation arguments later on.
    sightingId: String = "SIGHTING_ID_123",
    // 'onBack' is a function that will be called to navigate back (e.g., to the MapView)
    onBack: () -> Unit = {},
    viewModel: SightingDetailViewModel = viewModel()
) {
    val context = LocalContext.current
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
                    Firebase.analytics.logEvent("new_sighting") {
                        param("sightingId", sightingId)
                        param("timestamp", System.currentTimeMillis())
                        param("action", "detail_view_interaction")
                        param("source", "api36_emulator")
                    }

                    Toast.makeText(context, "Event logged – check DebugView", Toast.LENGTH_SHORT).show()
                    Log.d("FirebaseTest", "new_sighting → $sightingId")
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("Send to Firebase")
            }
        }
    ) { paddingValues ->
        // Use Column to stack details vertically
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Details for Sighting ID: $sightingId",
                style = MaterialTheme.typography.headlineMedium
            )
            // Placeholder for the image that will be loaded
            Text(text = "Image Placeholder ", style = MaterialTheme.typography.bodyLarge)

            // Placeholder for AI-identified animal name
            Text(text = "Animal Identified: ??? (AI API result)", style = MaterialTheme.typography.bodyMedium)

            // Placeholder for information from Wikipedia API
            Text(text = "Wikipedia Info: (Summary text about the animal)", style = MaterialTheme.typography.bodyMedium)

            // This is the functional part that allows you to talk about the data flow.
            Text(
                text = "This view will display data fetched from both Firestore (sighting data) and our researched APIs (AI identification & Wikipedia info).",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}