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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AsyncImage(
            model = photoUri,
            contentDescription = "Preview of photo",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            ProfileButton("Back", 1f, onDismiss)
            Spacer(modifier = Modifier.padding(24.dp))
            ProfileButton("Create", 1f, onConfirm)
        }
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
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    sighting: SightingMarker,
) {
    val currentDateTime = Date()


    val formatter: DateFormat = DateFormat.getDateTimeInstance(
        DateFormat.MEDIUM,
        DateFormat.MEDIUM,
        Locale.getDefault()
    )
    val formattedDateTime: String = formatter.format(currentDateTime)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = sighting.title + " - " + formattedDateTime) },

        text = {
            Column {
                AsyncImage(
                    model = sighting.imageUri,
                    contentDescription = "Sighting Photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .heightIn(max = 300.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Comment afaje;afj:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = sighting.comment,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },

        confirmButton = {
            TextButton(onClick = {
                onConfirm()
            }) {
                Text("Cancel")
            }
        },


    )
}