package com.example.group21.ui.sightingDetail

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.group21.PreviewButton
import com.example.group21.R
import com.example.group21.database.Sighting
import com.example.group21.database.SightingViewModel

@Composable
fun CardDetailsView(
    sightingViewModel: SightingViewModel,
    sighting: Sighting,
    onDismiss: () -> Unit,
    showInMap: (Sighting) -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD0000000))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .wrapContentHeight()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            AsyncImage(
                model = sighting.photoUrl.ifBlank { R.drawable.image_not_found },
                contentDescription = "Sighting Photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Fit
            )
            Row(
                modifier = Modifier.fillMaxWidth(0.8f).padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PreviewButton("Back", 1f, onDismiss)
                PreviewButton("Show In Map", 1f, {
                    showInMap(sighting) })
            }
        }
    }
}