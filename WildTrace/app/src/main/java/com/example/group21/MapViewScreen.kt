package com.example.group21

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState
import java.io.File
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.sp

@Composable
fun MapViewScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val vancouver = LatLng(49.2827, -123.1207)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(vancouver, 10f)
    }

    val colorScheme = MaterialTheme.colorScheme

    if (mapViewModel.showPhotoDialog.value) {
        PhotoPreviewDialog(
            photoUri = mapViewModel.imageUri.value!!,
            onConfirm = {
                mapViewModel.dismissPhotoDialog()
                navController.navigate("sightingDetail")
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

    val mapProperties = MapProperties(
        isMyLocationEnabled = true
    )

    Box(modifier = modifier.fillMaxSize().statusBarsPadding()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            onMapClick = { Toast.makeText(context, "Click", Toast.LENGTH_SHORT).show()}
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp, 16.dp, 16.dp, 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            FloatingActionButton(
                onClick = {
                    uri = createImageFile(context)
                    cameraLauncher.launch(uri)
                },
                modifier = Modifier
                    .padding(12.dp)
                    .size(width = 80.dp, height = 80.dp),
                containerColor = colorScheme.background,
                contentColor = colorScheme.onBackground
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = "Take a Photo",
                    modifier = Modifier.fillMaxSize(0.5f)
                )
            }

            // Instruction text below the button
            Text(
                text = "Click on the camera to begin",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 16.sp),
                color = colorScheme.onBackground,
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .background(
                        color = colorScheme.background,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            )
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