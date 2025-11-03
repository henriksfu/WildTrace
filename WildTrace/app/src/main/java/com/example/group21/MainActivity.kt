package com.example.group21

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.group21.ui.search.searchView.SearchView
import com.example.group21.ui.search.sightingDetail.SightingDetailView
import com.example.group21.ui.theme.WildTraceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WildTraceTheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "signup") {
        // --- Reid's Screens (Using Placeholders) ---
        composable("signup") {
            signupView(navController)
        }
        composable("login") {
            LoginView_Placeholder(navController)
        }

        // --- Steven's Screen (Using Placeholder) ---
        composable("map") {
            MapView_Placeholder(navController)
        }

        composable("search") {
            SearchView()
        }

        composable("sightingDetail") {
            SightingDetailView(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun signupView(navController: NavController) {
    //
    // Has access to the entered email and password
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        signupLoginInput(
            labelText = "Username",
            text = email,
            onChange = { newText -> email = newText }
        )
        signupLoginInput(
            labelText = "Password",
            text = password,
            onChange = { newText -> password = newText }
        )
    }
}

@Composable
fun signupLoginInput(labelText: String, text: String, onChange: (String) -> Unit) {

    val colorScheme = MaterialTheme.colorScheme

    OutlinedTextField(
        value = text,
        onValueChange = onChange,
        label = { Text(labelText) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colorScheme.surface,
            focusedIndicatorColor = colorScheme.primary,
            focusedLabelColor = colorScheme.primary,
            unfocusedContainerColor = colorScheme.primary.copy(alpha = 0.7f),
            unfocusedIndicatorColor = colorScheme.primary.copy(alpha = 0.7f),
            unfocusedLabelColor = colorScheme.primary.copy(alpha = 0.7f),
            cursorColor = colorScheme.primary,
        ),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    )
}

//Temporary Placeholders (can be removed later)
@Composable
fun SignUpView_Placeholder(navController: NavController) {
    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Sign Up Screen (Placeholder)")
            Button(onClick = { navController.navigate("login")}) { Text("Go to login")}
        }
    }
}

@Composable
fun LoginView_Placeholder(navController: NavController) {
    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("LOGIN SCREEN (Placeholder)")
            Button(onClick = { navController.navigate("map") }) { Text("Log In") }
        }
    }
}

@Composable
fun MapView_Placeholder(navController: NavController) {
    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text("MAP SCREEN (Placeholder)", modifier = Modifier.align(Alignment.Center))

            // --- Code to be completed by "Map & Sighting Flow Lead - STEVEN" ---
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { navController.navigate("search") }) {
                    Text("Go to Search (TEST)")
                }
                Button(onClick = { navController.navigate("sightingDetail") }) {
                    Text("Go to Detail (TEST)")
                }
            }
        }
    }
}