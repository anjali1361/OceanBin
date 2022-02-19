package com.nipun.oceanbin.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nipun.oceanbin.feature_oceanbin.presentation.screens.HomeScreen
import com.nipun.oceanbin.ui.theme.Screen
import com.nipun.oceanbin.ui.theme.SplashScreenTutTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplashScreenTutTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val mainViewModel: MainViewModel = hiltViewModel()
                    val isInstalled = mainViewModel.isInstalled.value

                    /* This nav controller is main screen navigator,
                     * We can use this to navigate like another activity in xml project
                     */
                    val navController = rememberNavController()
                    // Main Screen Nav Host
                    NavHost(
                        navController = navController,
                        // If app is just installed our splash screen will be shown else our main content will gonna shown.
                        startDestination = if(isInstalled)Screen.SplashScreen.route else Screen.HomeScreen.route
                    ) {
                        composable(route = Screen.SplashScreen.route) {

                            // Splash screen
                            SplashViewPager(navController = navController,mainViewModel)
                        }
                        composable(route = Screen.HomeScreen.route){
                            HomeScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}