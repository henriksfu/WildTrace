package com.example.group21

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Calendar
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
                    // TODO make future dates not selectable
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
                // TODO make future dates not selectable
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
    mapViewModel: MapViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    var selectedDateMillis by remember { mutableLongStateOf(Calendar.getInstance().timeInMillis) }
    var selectedTimeMillis by remember { mutableLongStateOf(Calendar.getInstance().timeInMillis) }
    var tempUri: Uri = Uri.EMPTY

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                imageUri = tempUri
                mapViewModel.setImageUri(imageUri!!)
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            imageUri = uri
        }
    )

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    tempUri = createImageFile(context)
                    cameraLauncher.launch(tempUri)
                }) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "Camera")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Camera")
                }

                Button(onClick = {
                    galleryLauncher.launch("image/*")
                }) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = "Gallery")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gallery")
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
                        // TODO insert bitmap instead of uri?
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