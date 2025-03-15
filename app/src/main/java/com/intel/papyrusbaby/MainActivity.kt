package com.intel.papyrusbaby

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.intel.papyrusbaby.screen.ArchivedLetterScreen
import com.intel.papyrusbaby.screen.HomeScreen
import com.intel.papyrusbaby.screen.WriteLetterScreen
import com.intel.papyrusbaby.screen.WrittenLetterScreen
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
                        composable(
                            route = "writtenLetter?writer={writer}&documentType={documentType}&prompt={prompt}",
                            arguments = listOf(
                                navArgument("writer") { defaultValue = "" },
                                navArgument("documentType") { defaultValue = "" },
                                navArgument("prompt") { defaultValue = "" }
                            )
                        ) { backStackEntry ->
                            val writer = backStackEntry.arguments?.getString("writer") ?: ""
                            val documentType =
                                backStackEntry.arguments?.getString("documentType") ?: ""
                            val prompt = backStackEntry.arguments?.getString("prompt") ?: ""
                            WrittenLetterScreen(
                                writer = writer,
                                documentType = documentType,
                                prompt = prompt,
                                navController = navController
                            )
                        }
                        composable("archive") { ArchivedLetterScreen(navController) }
                    }
                }, navController = navController)
            }
        }
    }
}
