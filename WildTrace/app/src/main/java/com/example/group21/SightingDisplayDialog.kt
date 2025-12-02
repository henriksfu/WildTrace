package com.example.group21

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun SightingDisplayDialog(
    sightingMarker: SightingMarker,
    onDismiss: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val colorScheme = MaterialTheme.colorScheme
    val formatter = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
    val timeStr = formatter.format(sightingMarker.sighting.sightingDateTime?.toDate()?: Date())
    //
    // Get the asynchronous image load
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(sightingMarker.sighting.photoUrl.takeIf { it.isNotBlank() })
        .error(R.drawable.image_not_found)
        .fallback(R.drawable.image_not_found)
        .build()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDD000000))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .wrapContentHeight()
                .verticalScroll(scrollState)
                .background(
                    color = colorScheme.background,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {

            Text(
                text = sightingMarker.sighting.animalName,
                color = colorScheme.onBackground,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp),
            )

            Text(
                text = timeStr,
                color = colorScheme.onBackground,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 16.dp),
            )

            AsyncImage(
                model = imageRequest,
                contentDescription = "Sighting Photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = sightingMarker.sighting.notes,
                color = colorScheme.onBackground,
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 16.dp),
            )

            PreviewButton("Back", 1f, onDismiss)
        }
    }
}