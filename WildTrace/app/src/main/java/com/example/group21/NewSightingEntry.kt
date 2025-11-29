package com.example.group21

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.drawable.Icon
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
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import com.example.group21.database.Sighting
import com.example.group21.database.SightingViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
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
    mapViewModel: MapViewModel,
    sightingViewModel: SightingViewModel,
) {
    var animalName by rememberSaveable { mutableStateOf("") }
    var comment by rememberSaveable { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    var selectedDateMillis by rememberSaveable { mutableLongStateOf(Calendar.getInstance().timeInMillis) }
    var selectedTimeMillis by rememberSaveable { mutableLongStateOf(Calendar.getInstance().timeInMillis) }
    var tempUri: Uri = Uri.EMPTY
    //
    // For saving the URI when the user takes a picture
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                imageUri = tempUri
                mapViewModel.setImageUri(imageUri!!)
            }
        }
    )
    //
    // for saving the image when the  user select a file
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
    //
    // Make scrollable
    val scrollState = rememberScrollState()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 25.dp, vertical = 25.dp)
    ){
        Text(
            text = "Create New Sighting",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 28.sp,
            modifier = Modifier.padding(top = 8.dp),
        )
        entryInput(
            "Animal Name",
            animalName,
            {newName -> animalName = newName}
        )
        Row(
            modifier = Modifier.padding(horizontal = 25.dp)
        ) {
            EntryButtonWithIcon("Camera", 0.65f, "Camera", {
                navController.navigate("login")
            })
            EntryButtonWithIcon("Gallery", 0.65f, "Gallery", {
                //
            })
        }
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
                        val sighting = Sighting(
                            animalName,
                            "",
                            1,
                            GeoPoint(lat?.toDouble() ?: 0.0, lng?.toDouble() ?: 0.0),
                            "",
                            imageUri.toString(),
                            FirebaseAuth.getInstance().currentUser?.displayName ?: "Anonymous",
                            FirebaseAuth.getInstance().currentUser?.uid ?: "",
                            Timestamp.now()
                        )
                        Log.e("save", "made sighting")
                        sightingViewModel.saveSighting(sighting)
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

@Composable
fun entryInput(labelText: String, text: String, onChange: (String) -> Unit) {

    val colorScheme = MaterialTheme.colorScheme

    OutlinedTextField(
        value = text,
        onValueChange = onChange,
        label = { Text(labelText) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colorScheme.background,
            unfocusedContainerColor = colorScheme.background,
            focusedIndicatorColor = colorScheme.primary,
            unfocusedIndicatorColor = colorScheme.onBackground,
            focusedLabelColor = colorScheme.primary,
            unfocusedLabelColor = colorScheme.onBackground,
            focusedTextColor = colorScheme.primary,
            unfocusedTextColor = colorScheme.onBackground,
            cursorColor = colorScheme.onBackground
        ),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    )
}

@Composable
fun EntryButtonWithIcon(text: String, alpha: Float, icon: String, onClick: () -> Unit) {
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
        shape = RoundedCornerShape(16.dp),
    ) {
        Icon(
            imageVector =
                when (icon){
                    "Camera" -> Icons.Filled.PhotoCamera
                    "Gallery" -> Icons.Filled.FileUpload
                    else -> Icons.Filled.QuestionMark
                },
            contentDescription = "Button Icon: Gallery or Camera"
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            modifier = Modifier.padding(8.dp),
            fontSize = 16.sp,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun EntryButton(text: String, alpha: Float, onClick: () -> Unit) {
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
            fontSize = 16.sp,
            style = MaterialTheme.typography.labelLarge
        )
    }
}