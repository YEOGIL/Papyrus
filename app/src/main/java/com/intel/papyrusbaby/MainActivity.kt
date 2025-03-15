package com.intel.papyrusbaby

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.intel.papyrusbaby.screen.ArchivedLetterScreen
import com.intel.papyrusbaby.screen.HomeScreen
import com.intel.papyrusbaby.screen.WriteLetterScreen
import com.intel.papyrusbaby.ui.theme.PapyrusBabyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PapyrusBabyTheme {
                val navController = rememberNavController()
                AppBar(content = { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable("home") { HomeScreen(navController) }
                        composable("write") { WriteLetterScreen(navController) }
                        composable("archive") { ArchivedLetterScreen(navController) }
                    }
                }, navController = navController)
            }
        }
    }
}
