package com.example.group21.ui.search.searchView

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.group21.database.Sighting
import com.example.group21.database.SightingViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.example.group21.R
import com.example.group21.ui.sightingDetail.CardDetailsView

@Composable
fun SearchView(
    navController: NavController,
    sightingViewModel: SightingViewModel,
    onResultClick: (String) -> Unit = {} // needed for navigation to detail later
) {
    //
    // all sightings state from viewModel
    val allSightings by sightingViewModel.allSightings.observeAsState(emptyList())
    //
    // Start loading the sightings
    LaunchedEffect(Unit) {
        sightingViewModel.loadAllSightings()
    }
    //
    // State so the gridview onclick gets the right sighting
    var selectedSighting by remember { mutableStateOf<Sighting?>(null) }
    //
    // Needed to save the query for searching
    var query by remember { mutableStateOf("") }
    //
    // To make the view scrollable
    val scrollState = rememberScrollState()
    //
    // Some test sightings
    val cat = Sighting(
        "cat",
        "scientific cat",
        1,
        GeoPoint(0.0, 0.0),
        "note",
        "",
        FirebaseAuth.getInstance().currentUser?.displayName ?: "Anonymous",
        FirebaseAuth.getInstance().currentUser?.uid ?: "",
        Timestamp.now()
    )
    val bat = Sighting(
        "bat",
        "scientific bat",
        1,
        GeoPoint(0.0, 0.0),
        "note",
        "",
        FirebaseAuth.getInstance().currentUser?.displayName ?: "Anonymous",
        FirebaseAuth.getInstance().currentUser?.uid ?: "",
        Timestamp.now()
    )
    val rat = Sighting(
        "rat",
        "scientific rat",
        1,
        GeoPoint(0.0, 0.0),
        "note",
        "",
        FirebaseAuth.getInstance().currentUser?.displayName ?: "Anonymous",
        FirebaseAuth.getInstance().currentUser?.uid ?: "",
        Timestamp.now()
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Search",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                modifier = Modifier.padding(top = 8.dp),
            )

            SearchInput("Search For an Animal", query, { n -> query = n })

            SearchButtonWithIcon("Search", 1f, "Search", {
                sightingViewModel.loadFilteredSightings(query)
            })

            SearchButton("Wipe Sightings", 1f, {
                sightingViewModel.wipeAllSightings()
            })
            Row() {
                SearchButton("Cat", 1f,  {
                    sightingViewModel.saveSighting(cat)
                })
                SearchButton("Bat", 1f,  {
                    sightingViewModel.saveSighting(bat)
                })
                SearchButton("Rat", 1f,  {
                    sightingViewModel.saveSighting(rat)
                })
            }
            //
            // Set up the recycler view
            SightingList(allSightings, 2, selectedSighting, {
                item -> selectedSighting = item
            })
        }
    }
    //
    // Shows the popup when a sighting is clicked on
    if (selectedSighting != null) {
        CardDetailsView(
            sightingViewModel = sightingViewModel,
            sighting = selectedSighting!!,
            onDismiss = { selectedSighting = null },
            showInMap = {

            }
        )
    }
}

@Composable
fun SightingList(
    items: List<Sighting>,
    columns: Int = 2,
    selectedSighting: Sighting?,
    onClick: (Sighting) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier
            .padding(vertical = 12.dp)
            .fillMaxSize()
            .heightIn(min = 200.dp, max = 500.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(
            items = items,
            key = { it.documentId?: it.hashCode().toString() }
        ) { s ->
            SightingCard(s, Modifier, { onClick(s) } )
        }
    }
}

@Composable
fun SearchInput(
    labelText: String,
    text: String, onChange: (String) -> Unit) {

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
            .widthIn(min = 500.dp, max = 650.dp)
            .padding(vertical = 16.dp, horizontal = 8.dp)
    )
}

@Composable
fun SearchButtonWithIcon(text: String, alpha: Float, icon: String, onClick: () -> Unit) {
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
                    "Search" -> Icons.Filled.Search
                    "Back"   -> Icons.AutoMirrored.Filled.ArrowBack
                    else -> Icons.Filled.QuestionMark
                },
            contentDescription = "Button Icon: Search"
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
fun SearchButton(text: String, alpha: Float, onClick: () -> Unit) {
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
        Text(
            text = text,
            modifier = Modifier.padding(4.dp),
            fontSize = 16.sp,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun SightingCard(
    sighting: Sighting,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    //
    //  Make date string to display
    val date = if (sighting.createdAt==null) java.util.Date() else sighting.createdAt.toDate()
    val formatter = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
    val displayDate = formatter.format(date)

    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            val painter = rememberAsyncImagePainter(
                model = sighting.photoUrl.ifBlank { R.drawable.image_not_found },
                error = painterResource(R.drawable.image_not_found)
            )
            Image(
                painter = painter,
                contentDescription = sighting.animalName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = sighting.animalName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }
        }
    }
}
