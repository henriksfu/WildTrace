package com.example.group21.ui.search.searchView

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.group21.EntryButtonWithIcon
import com.example.group21.createImageFile
import com.example.group21.database.Sighting
import com.example.group21.database.SightingViewModel

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
    // Needed to save the query for searching
    var query by remember { mutableStateOf("") }
    //
    // Collect state from ViewModel
    var filtersShown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(vertical = 24.dp, horizontal = 16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Search",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 28.sp,
            modifier = Modifier.padding(vertical = 16.dp),
        )

        SearchInput("Search For an Animal", query, {n -> query = n})

        Row(
            modifier = Modifier.padding(horizontal = 5.dp)
        ) {
            SearchButtonWithIcon("Filter", 1f, "Filter", {
                filtersShown = true
            })
            SearchButtonWithIcon("Search", 1f, "Search", {

            })
        }

        Text(
            text = "All Sightings",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
        //
        // Set up the recycler view
        SightingList(allSightings, 4)
        //
        // Back button
        SearchButton("Back", 1f,{navController.popBackStack()})
    }
}

@Composable
fun SightingList(
    items: List<Sighting>,
    columns: Int = 4
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Blue)
            .heightIn(min = 200.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(
            items = items,
            key = { index -> index }
        ) { item ->
            SightingCard(item, {})
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
            .fillMaxWidth()
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
                    "Filter" -> Icons.Filled.Settings
                    "Search" -> Icons.Filled.Search
                    else -> Icons.Filled.QuestionMark
                },
            contentDescription = "Button Icon: Filter or Search"
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

// Reusable card for each search result
@Composable
fun SightingCard(
    sighting: Sighting,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = sighting.animalName)
        }
    }
}
