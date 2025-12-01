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
import androidx.compose.ui.graphics.Color
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
    val colorScheme = MaterialTheme.colorScheme
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
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }
    }

    Button(
        onClick = { datePickerDialog.show() },
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.primary,
            contentColor = colorScheme.onBackground,
            disabledContainerColor = colorScheme.primary,
            disabledContentColor = colorScheme.onBackground
        )
    ) {
        Text("${dateFormat.format(calendar.time)}")
    }
}

@Composable
fun TimePickerButton(
    timeMillis: Long,
    onTimeSelected: (Long) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { timeInMillis = timeMillis }
    val colorScheme = MaterialTheme.colorScheme
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    val timePickerDialog = remember {
        TimePickerDialog(
            context, { _, hour, minute ->
                val now = Calendar.getInstance()
                val selected = Calendar.getInstance().apply {
                    timeInMillis = timeMillis
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                //
                // If time selected was in teh future clamp it to the present
                if (selected.after(now)) {
                    selected.timeInMillis = now.timeInMillis
                }
                onTimeSelected(selected.timeInMillis)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
    }

    Button(
        onClick = { timePickerDialog.show() },
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.primary,
            contentColor = colorScheme.onBackground,
            disabledContainerColor = colorScheme.primary,
            disabledContentColor = colorScheme.onBackground
        )
    ) {
        Text("${timeFormat.format(calendar.time)}")
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
    var errorMsg by rememberSaveable { mutableStateOf("") }
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
                mapViewModel.setImageUri(imageUri!!, false)
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
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
            modifier = Modifier
                .widthIn(max = 600.dp)
                .verticalScroll(scrollState)
                .padding(start = 20.dp, top = 25.dp, end = 20.dp, bottom = 25.dp)
        ) {
            Text(
                text = "Create New Sighting",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                modifier = Modifier.padding(vertical = 16.dp),
            )
            entryInput(
                "Animal Name",
                animalName,
                { newName -> animalName = newName }
            )
            Text(
                text = "Sighting Photo",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            //
            // If there is an image, display it, otherwise placeholder text
            (
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Selected Sighting Photo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(align = Alignment.CenterVertically)
                                .heightIn(max = 250.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No Photo Selected",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 16.sp,
                            )
                        }
                    }
                    )
            Spacer(modifier = Modifier.padding(4.dp))
            //
            Row(
                modifier = Modifier.padding(horizontal = 5.dp)
            ) {
                EntryButtonWithIcon("Camera", 1f, "Camera", {
                    tempUri = createImageFile(context)
                    cameraLauncher.launch(tempUri)
                })
                EntryButtonWithIcon("Gallery", 1f, "Gallery", {
                    galleryLauncher.launch("image/*")
                })
            }

            //
            // Show the date and time of this entry
            dateAndTimeButtons(
                selectedDateMillis = selectedDateMillis,
                onDateSelected = { new -> selectedDateMillis = new },
                selectedTimeMillis = selectedTimeMillis,
                onTimeSelected = { new -> selectedTimeMillis = new }
            )
            //
            entryInput(
                "Comment",
                comment,
                { c -> comment = c }
            )
            Text(
                text = errorMsg,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(4.dp),
            )
            Row(
                modifier = Modifier.padding(horizontal = 5.dp)
            ) {
                EntryButton("Cancel", 0.7f, {
                    navController.popBackStack()
                })
                EntryButton("Save Entry", 1f, {
                    //
                    // Check if data is valid
                    if (animalName == "") {
                        errorMsg = "Please name the animal you saw."
                    } else if (imageUri == null) {
                        errorMsg = "Please take an image or upload one."
                    } else {
                        //
                        // insert into database
                        val sighting = Sighting(
                            animalName,
                            "",
                            1,
                            GeoPoint(lat?.toDouble() ?: 0.0, lng?.toDouble() ?: 0.0),
                            comment,
                            imageUri.toString(),
                            FirebaseAuth.getInstance().currentUser?.displayName ?: "Anonymous",
                            FirebaseAuth.getInstance().currentUser?.uid ?: "",
                            Timestamp.now()
                        )
                        sightingViewModel.saveSighting(sighting)
                        navController.popBackStack()
                    }
                })
            }
        }
    }
}

@Composable
fun dateAndTimeButtons(
    selectedDateMillis: Long,
    onDateSelected: (Long) -> Unit,
    selectedTimeMillis: Long,
    onTimeSelected: (Long) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        modifier = Modifier
            .widthIn(max = 500.dp)
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Date of Sighting:",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            DatePickerButton(
                dateMillis = selectedDateMillis,
                onDateSelected = onDateSelected,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Time of Sighting:",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            TimePickerButton(
                timeMillis = selectedTimeMillis,
                onTimeSelected = onTimeSelected
            )
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
            .padding(4.dp)
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
            .padding(4.dp),
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
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            modifier = Modifier.padding(4.dp),
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
            .padding(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.tertiary.copy(alpha=alpha),
            contentColor = colorScheme.onBackground,
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(4.dp),
            fontSize = 16.sp,
            style = MaterialTheme.typography.labelLarge
        )
    }
}