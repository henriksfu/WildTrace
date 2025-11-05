package com.example.group21

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import java.io.File
import java.util.Date
import java.util.Locale

@Composable
fun MapViewScreen(navController: NavController,
                  modifier: Modifier = Modifier,
                  mapViewModel: MapViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val singapore = LatLng(49.2827, -123.1207)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }


    if (mapViewModel.showPhotoDialog.value) {
        PhotoPreviewDialog(
            photoUri = mapViewModel.imageUri.value!!,
            onConfirm = {
                mapViewModel.dismissPhotoDialog()
            },
            onDismiss = {
                mapViewModel.dismissPhotoDialog()
            }
        )
    }

    var uri: Uri = Uri.EMPTY

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                mapViewModel.setImageUri(uri)
            }
        }
    )

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 20.dp
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        )

        {
            FloatingActionButton(
                onClick = {
                    uri = createImageFile(context)
                    cameraLauncher.launch(uri)
                },
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = "Take a Photo"
                )
            }

            Button(onClick = { navController.navigate("search") }) {
                Text("Go to Search (TEST)")
            }
            Button(onClick = { navController.navigate("sightingDetail") }) {
                Text("Go to Detail (TEST)")
            }
        }
    }
}

private fun createImageFile(context: Context): Uri {

    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CANADA).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"

    val storageDir = context.externalCacheDir
    val image = File.createTempFile(
        imageFileName,
        ".jpg",
        storageDir
    )

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        image
    )
}