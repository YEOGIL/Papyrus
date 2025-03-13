package com.intel.papyrusbaby

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.intel.papyrusbaby.screen.HomeScreen
import com.intel.papyrusbaby.screen.WriteLetterScreen
import com.intel.papyrusbaby.ui.theme.PapyrusBabyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PapyrusBabyTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination ="home",
                    // if (viewmodel.profiles.isEmpty()) "main" else "profile",
                ) {
                    composable("home") {HomeScreen(navController)}
                    composable("write") {WriteLetterScreen(navController)}
                }
            }
        }
    }
}
