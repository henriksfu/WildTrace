package com.example.group21


import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.group21.database.Sighting
import com.example.group21.database.SightingViewModel
import com.example.group21.ui.sightingDetail.dateDisplay
import java.text.DateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date
import java.util.Locale

@Composable
fun PhotoPreviewDialog(
    photoUri: Uri,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
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
                model = photoUri,
                contentDescription = "Preview of photo",
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
                PreviewButton("Analyze", 1f, onConfirm)
            }
        }
    }
}

@Composable
fun PreviewButton(text: String, alpha: Float, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    Button(
        onClick = onClick,
        modifier = Modifier
            .wrapContentWidth(Alignment.CenterHorizontally)
            .wrapContentHeight()
            .padding(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.tertiary.copy(alpha=alpha),
            contentColor = colorScheme.onBackground,
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(8.dp),
            fontSize = 18.sp,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
/*
@Composable
fun PhotoPreviewDialog(
    photoUri: Uri,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Review Sighting Photo") },
        text = {
            AsyncImage(
                model = photoUri,
                contentDescription = "Preview of photo",
                modifier = modifier
                    .fillMaxWidth(0.8f)
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss ) {
                Text("Cancel")
            }
        }
    )
}
*/

@Composable
fun SightingDisplayDialog(
    sightingViewModel: SightingViewModel,
    sightingMarker: SightingMarker,
    onDismiss: () -> Unit,
) {
    val scrollState = rememberScrollState()

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
                model = imageRequest,
                contentDescription = "Sighting Photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Fit
            )

            dateDisplay()

            Text(
                text = sightingMarker.sighting.notes,
                color = Color.White,
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 16.dp),
            )

            PreviewButton("Back", 1f, onDismiss)
        }
    }
}