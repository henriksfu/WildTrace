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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.group21.PreviewButton
import com.example.group21.R
import com.example.group21.database.Sighting
import com.example.group21.database.SightingViewModel
import java.util.Date

@Composable
fun CardDetailsView(
    sightingViewModel: SightingViewModel,
    sighting: Sighting,
    onDismiss: () -> Unit,
    showInMap: (Sighting) -> Unit
) {
    val scrollState = rememberScrollState()
    val colorScheme = MaterialTheme.colorScheme
    //
    // Get the asynchronous image load
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(sighting.photoUrl.takeIf { it.isNotBlank() })
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
                text = sighting.animalName,
                color = colorScheme.onBackground,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp),
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

            dateDisplay()

            Text(
                text = sighting.notes,
                color = colorScheme.onBackground,
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 16.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(0.8f).padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PreviewButton("Back", 1f, onDismiss)
                PreviewButton("Locate", 1f, {
                    showInMap(sighting) })
            }
        }
    }
}

@Composable
fun dateDisplay(
    // date: Date
){

    val colorScheme = MaterialTheme.colorScheme

    Button(
        onClick = {  },
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.primary,
            contentColor = colorScheme.onBackground,
            disabledContainerColor = colorScheme.primary,
            disabledContentColor = colorScheme.onBackground
        ),
    ) {
        Text(
            text = "TODO"
        )
    }
}
