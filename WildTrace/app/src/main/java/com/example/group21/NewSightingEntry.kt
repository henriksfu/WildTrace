package com.example.group21

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.group21.database.Sighting
import com.example.group21.database.SightingViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DatePickerButton(
    dateMillis: Long,
    onDateSelected: (Long) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onDateSelected(newCalendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Button(onClick = { datePickerDialog.show() }) {
        Text("Date: ${dateFormat.format(calendar.time)}")
    }
}

@Composable
fun TimePickerButton(
    timeMillis: Long,
    onTimeSelected: (Long) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { timeInMillis = timeMillis }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                val newCalendar = Calendar.getInstance().apply {
                    timeInMillis = timeMillis
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }
                onTimeSelected(newCalendar.timeInMillis)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
    }

    Button(onClick = { timePickerDialog.show() }) {
        Text("Time: ${timeFormat.format(calendar.time)}")
    }
}

@Composable
fun NewSightingEntry(
    navController: NavController,
    lat: Float?,
    lng: Float?,
    mapViewModel: MapViewModel,
    sightingViewModel: SightingViewModel,
) {
    var title by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf(mapViewModel.imageUri.value) }
    val context = LocalContext.current
    var selectedDateMillis by remember { mutableLongStateOf(Calendar.getInstance().timeInMillis) }
    var selectedTimeMillis by remember { mutableLongStateOf(Calendar.getInstance().timeInMillis) }

    // Helper to store the temp URI for the camera
    var tempUri by remember { mutableStateOf(Uri.EMPTY) }

    // 1. Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                imageUri = tempUri
                mapViewModel.setImageUri(imageUri!!)
            }
        }
    )

    // 2. Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                imageUri = uri
                mapViewModel.setImageUri(uri)
            }
        }
    )

    if (mapViewModel.showPhotoDialog.value && mapViewModel.imageUri.value != null) {
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

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Sighting Entry",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    )
    { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Animal Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Sighting Photo",
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val imageModifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .aspectRatio(16f / 9f)

            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Selected Sighting Photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .heightIn(max = 300.dp)
                )
            } else {
                Box(
                    modifier = imageModifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Photo Selected", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Two Buttons: Camera & Gallery ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    // ✅ RENAMED FUNCTION CALL
                    tempUri = createSightingImage(context)
                    cameraLauncher.launch(tempUri)
                }) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "Camera")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Camera")
                }

                Button(onClick = {
                    // Open the Gallery
                    galleryLauncher.launch("image/*")
                }) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = "Gallery")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gallery")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- ✅ Identify Button (Only shows if photo exists) ---
            if (imageUri != null) {
                Button(
                    onClick = {
                        try {
                            // 1. Convert the URI to a Bitmap
                            val inputStream = context.contentResolver.openInputStream(imageUri!!)
                            val bitmap = BitmapFactory.decodeStream(inputStream)

                            // 2. Save it to our singleton Holder
                            ImageHolder.capturedImage = bitmap

                            // 3. Navigate to the Detail View for AI Analysis
                            navController.navigate("sightingDetail")
                        } catch (e: Exception) {
                            Log.e("NewSightingEntry", "Error converting image", e)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("✨ Identify Animal with AI")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Date of Sighting:", modifier = Modifier.weight(1f))
                DatePickerButton(
                    dateMillis = selectedDateMillis,
                    onDateSelected = { selectedDateMillis = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Time of Sighting:", modifier = Modifier.weight(1f))
                TimePickerButton(
                    timeMillis = selectedTimeMillis,
                    onTimeSelected = { selectedTimeMillis = it }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Comment") },
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        // insert into database
                        val sighting = Sighting(
                            title,
                            "",
                            1,
                            GeoPoint(lat?.toDouble()?:0.0, lng?.toDouble()?:0.0),
                            "",
                            imageUri.toString(),
                            FirebaseAuth.getInstance().currentUser?.displayName ?: "Anonymous",
                            FirebaseAuth.getInstance().currentUser?.uid ?: "",
                            Timestamp.now()
                        )
                        Log.e("save","made sighting")
                        sightingViewModel.saveSighting(sighting)
                        mapViewModel.addSighting(
                            title,
                            comment,
                            imageUri,
                            selectedDateMillis,
                            selectedTimeMillis,
                            lat,
                            lng
                        )
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Sighting")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun createSightingImage(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val image = File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        storageDir
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        image
    )
}