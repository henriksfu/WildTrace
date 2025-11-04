package com.example.group21

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            WildTraceTheme(dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        // --- Reid's Screens (Using Placeholders) ---
        composable("login") {
            loginView(navController)
        }
        composable("signup") {
            SignUpView_Placeholder(navController)
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
fun loginView(navController: NavController) {
    //
    // Has access to the entered email and password
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
            .padding(horizontal = 25.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.wildtrace_logo),
            contentDescription = "App logo",
            modifier = Modifier
                .size(width = 350.dp, height = 200.dp)
                .padding(horizontal = 8.dp, vertical = 0.dp),
            contentScale = ContentScale.FillWidth
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Text(
            text = "Login",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 28.sp,
            modifier = Modifier.padding(top = 8.dp),
        )
        Spacer(modifier = Modifier.padding(4.dp))
        signupLoginInput(
            labelText = "Email",
            text = email,
            onChange = { newText -> email = newText }
        )
        signupLoginInput(
            labelText = "Password",
            text = password,
            onChange = { newText -> password = newText }
        )
        Row(
            modifier = Modifier.padding(horizontal = 25.dp)
        ) {
            profileButton("Log In", {})
            profileButton("Sign Up", {
                navController.navigate("signup")
            })
        }
    }
}

@Composable
fun profileButton(text: String, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    Button(
        onClick = onClick,
        modifier = Modifier
            .wrapContentWidth(Alignment.CenterHorizontally)
            .wrapContentHeight()
            .padding(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.tertiary,
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
@Composable
fun signupLoginInput(labelText: String, text: String, onChange: (String) -> Unit) {

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