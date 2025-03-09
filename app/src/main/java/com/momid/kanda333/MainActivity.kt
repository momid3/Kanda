package com.momid.kanda333

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.momid.kanda333.ui.InitialScreen
import com.momid.kanda333.ui.LoginScreen
import com.momid.kanda333.ui.MapScreen
import com.momid.kanda333.ui.RegistrationScreen
import com.momid.kanda333.ui.theme.Kanda333Theme
import com.momid.kanda333.view_model.AuthViewModel
import com.momid.kanda333.view_model.LocationViewModel
import com.momid.kanda333.database.AppDatabase

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        setContent {
            Kanda333Theme {
                AppNavigation(fusedLocationClient)
            }
        }
    }
}

@Composable
fun AppNavigation(fusedLocationClient: FusedLocationProviderClient) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val dataStore = remember { DataStore(context) }
    val authViewModel: AuthViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(db.userDao(), dataStore) as T
        }
    })
    val locationViewModel: LocationViewModel =
        viewModel(factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LocationViewModel(db.locationDao()) as T
            }
        })

    val startDestination = remember {
        if (dataStore.isLoggedIn()) "map" else "initial"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("initial") {
            InitialScreen(
                onLoginClick = { navController.navigate("login") },
                onSignUpClick = { navController.navigate("registration") }
            )
        }
        composable("registration") {
            RegistrationScreen(authViewModel) {
                navController.navigate("login")
            }
        }
        composable("login") {
            LoginScreen(authViewModel) {
                navController.navigate("map")
            }
        }
        composable("map") {
            MapScreen(authViewModel, locationViewModel, fusedLocationClient)
        }
    }
}
