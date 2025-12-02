package com.example.group21

import com.example.group21.Util
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap // ✅ ADDED
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.group21.database.Sighting
import com.example.group21.database.SightingViewModel
import com.example.group21.ui.search.searchView.SearchView
import com.example.group21.ui.search.sightingDetail.SightingDetailView
import com.example.group21.ui.theme.WildTraceTheme
import com.google.firebase.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics

// ✅ NEW: A temporary holder for the image to pass it between screens
object ImageHolder {
    var capturedImage: Bitmap? = null
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val analytics = Firebase.analytics
        analytics.setAnalyticsCollectionEnabled(true)

        if (BuildConfig.DEBUG) {
            analytics.setUserProperty("debug_mode", "emulator_api36")
            Log.d("FirebaseInit", "Debug mode ON – API 36 emulator")
        }

        enableEdgeToEdge()
        Util.checkPermissions(this)
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

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun AppNavigation(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = "map",
        route = "graph" // Standardized main navigation route name
    ) {

        // --- Authentication Screens ---
        composable("login") { backStackEntry ->
            val authEntry = navController.getBackStackEntry("graph")
            val authViewModel: AuthViewModel = viewModel(authEntry)
            LoginView(navController, authViewModel)
        }

        composable(
            "locate_sighting/{sightingId}",
            arguments = listOf(
                navArgument("sightingId") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val sightingId = backStackEntry.arguments?.getString("sightingId")

            val graphEntry = navController.getBackStackEntry("graph")
            val sightingViewModel: SightingViewModel = viewModel(graphEntry)

            val sighting: Sighting? = sightingViewModel.getSighting(sightingId?:"")
            if(sighting != null){
                LocateSightingView(sighting)
            }
        }

        composable("signup") { backStackEntry ->
            val authEntry = navController.getBackStackEntry("graph")
            val authViewModel: AuthViewModel = viewModel(authEntry)
            SignupView(navController, authViewModel)
        }

        // --- Map Screen (Conflict Resolved) ---
        composable("map") { backStackEntry ->
            // ViewModels scoped to the main graph level
            val graphEntry = navController.getBackStackEntry("graph")
            val mapViewModel: MapViewModel = viewModel(graphEntry)
            val sightingViewModel: SightingViewModel = viewModel(graphEntry)

            val context = LocalContext.current
            val notFineLocation =
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            val notCoarseLocation =
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED

            if (notFineLocation && notCoarseLocation) {
                Toast.makeText(context, "Please grant location permissions to view this screen", Toast.LENGTH_LONG).show()
            } else {
                MapViewScreen(
                    navController = navController,
                    mapViewModel = mapViewModel,
                    sightingViewModel = sightingViewModel
                )
            }
        }

        // --- New Sighting Screen (Conflict Resolved) ---
        composable(
            route = "sighting/{latitude}/{longitude}",
            arguments = listOf(
                navArgument("latitude") { type = NavType.FloatType },
                navArgument("longitude") { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            // make sure its same viewmodel instances
            val graphEntry = navController.getBackStackEntry("graph")
            val mapViewModel: MapViewModel = viewModel(graphEntry)
            val sightingViewModel: SightingViewModel = viewModel(graphEntry)

            val latitude = backStackEntry.arguments?.getFloat("latitude")
            val longitude = backStackEntry.arguments?.getFloat("longitude")

            NewSightingEntry(
                navController = navController,
                lat = latitude,
                lng = longitude,
                mapViewModel = mapViewModel,
                sightingViewModel = sightingViewModel
            )
        }

        composable("search") { backStackEntry ->
            val authEntry = navController.getBackStackEntry("graph")
            val sightingViewModel: SightingViewModel = viewModel(authEntry)
            SearchView(navController, sightingViewModel, {})
        }

        // --- Sighting Detail ---
        composable("sightingDetail") {
            SightingDetailView(
                capturedImage = ImageHolder.capturedImage, // Pass the singleton image
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// --- Login & Signup Views (Kept exactly as original) ---

@Composable
fun LoginView(navController: NavController,
              viewModel: AuthViewModel) {

    val email    by viewModel.email
    val password by viewModel.password
    val errorMsg by viewModel.errorMessage

    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 25.dp, vertical = 25.dp)
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
        LoginInput(
            labelText = "Email",
            text = email,
            onChange = viewModel::onEmailChange
        )
        LoginInput(
            labelText = "Password",
            text = password,
            onChange = viewModel::onPasswordChange
        )
        Text(
            text = errorMsg,
            color = Color.Red,
            fontSize = 14.sp,
            modifier = Modifier.padding(4.dp),
        )
        Row(
            modifier = Modifier.padding(horizontal = 25.dp)
        ) {
            ProfileButton("Log In", 1f, {
                viewModel.login({navController.navigate("map")})
            })
            ProfileButton("Sign Up", 1f, {
                navController.navigate("signup")
            })
        }
        Spacer(modifier = Modifier.padding(15.dp))
    }
}

@Composable
fun ProfileButton(text: String, alpha: Float, onClick: () -> Unit) {
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
@Composable
fun LoginInput(labelText: String, text: String, onChange: (String) -> Unit) {

    val colorScheme = MaterialTheme.colorScheme

    var passwordHidden: Boolean by remember { mutableStateOf(true) }

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
        visualTransformation =
            if (passwordHidden && labelText == "Password") PasswordVisualTransformation() else VisualTransformation.None,
        leadingIcon = if (labelText == "Email") {
            { Icon(Icons.Filled.Email,
                contentDescription = "Email",
                modifier = Modifier.size(20.dp)
            )}
        } else if (labelText == "Password") {
            { Icon(Icons.Filled.Lock,
                contentDescription = "Password",
                modifier = Modifier.size(20.dp)
            )}
        } else null,
        trailingIcon = {
            if(labelText == "Password") {
                var icon =  if (!passwordHidden) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                var description = if (!passwordHidden) "Hide password" else "Show password"

                IconButton(onClick = { passwordHidden = !passwordHidden }) {
                    Icon(imageVector = icon, contentDescription = description)
                }
            }
            else null
        },
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    )
}

@Composable
fun SignupInput(labelText: String, text: String, onChange: (String) -> Unit) {

    val colorScheme = MaterialTheme.colorScheme

    var passwordHidden: Boolean by remember { mutableStateOf(true) }

    OutlinedTextField(
        value = text,
        onValueChange = onChange,
        label = null,
        placeholder = { Text(labelText) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colorScheme.background,
            unfocusedContainerColor = colorScheme.background,
            focusedIndicatorColor = colorScheme.primary,
            unfocusedIndicatorColor = colorScheme.onBackground,
            focusedPlaceholderColor = colorScheme.primary,
            unfocusedPlaceholderColor = colorScheme.onBackground,
            focusedTextColor = colorScheme.primary,
            unfocusedTextColor = colorScheme.onBackground,
            cursorColor = colorScheme.onBackground
        ),
        visualTransformation =
            if (passwordHidden && labelText == "Password") PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = {
            if(labelText == "Password") {
                var icon =  if (!passwordHidden) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                var description = if (!passwordHidden) "Hide password" else "Show password"

                IconButton(onClick = { passwordHidden = !passwordHidden }) {
                    Icon(imageVector = icon, contentDescription = description)
                }
            }
            else null
        },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    )
}

@Composable
fun SignupView(navController: NavController,
               viewModel: AuthViewModel) {
    val email    by viewModel.email
    val password by viewModel.password
    val fName    by viewModel.fName
    val lName    by viewModel.lName
    val errorMsg by viewModel.errorMessage

    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(15.dp, Alignment.Top),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 25.dp)
            .padding(top = 100.dp, bottom = 25.dp)
    ) {
        Text(
            text = "Create New Account",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 30.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 25.dp),
        )
        SignupInput(
            labelText = "First Name",
            text = fName,
            onChange = viewModel::onfNameChange
        )
        SignupInput(
            labelText = "Last Name",
            text = lName,
            onChange = viewModel::onlNameChange
        )
        SignupInput(
            labelText = "Email",
            text = email,
            onChange = viewModel::onEmailChange
        )
        SignupInput(
            labelText = "Password",
            text = password,
            onChange = viewModel::onPasswordChange
        )
        Text(
            text = errorMsg,
            color = Color.Red,
            fontSize = 14.sp,
        )
        Row(
            modifier = Modifier.padding(horizontal = 25.dp)
        ) {
            ProfileButton("Back", 0.7f, {
                viewModel.resetStates()
                navController.navigate("login")
            })
            ProfileButton("Create", 1f) {
                viewModel.createProfile {
                    navController.navigate("map")
                }
            }
        }
    }
}

@Composable
fun ProfileView(navController: NavController,
                viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val email    by viewModel.email
    val password by viewModel.password
    val fName    by viewModel.fName
    val lName    by viewModel.lName

    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(25.dp, Alignment.Top),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(top = 100.dp, bottom = 25.dp)
    ){
        Text(
            text = "Profile Details",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 30.sp,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = "Profile Details",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 30.sp,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}