package com.intel.papyrusbaby

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.intel.papyrusbaby.firebase.AuthScreen
import com.intel.papyrusbaby.ui.theme.PapyrusBabyTheme
import com.intel.papyrusbaby.screen.ArchivedLetterScreen
import com.intel.papyrusbaby.screen.HomeScreen
import com.intel.papyrusbaby.screen.WriteLetterScreen
import com.intel.papyrusbaby.screen.WrittenLetterScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PapyrusBabyTheme {
                val navController = rememberNavController()
                // 로그인 상태를 Boolean으로 관리 (Firebase.auth.currentUser 이용)
                var isLoggedIn by remember { mutableStateOf(Firebase.auth.currentUser != null) }

                // Logout callback: Firebase.auth.signOut() 후 로그인 상태를 false로 설정
                val onLogout: () -> Unit = {
                    Firebase.auth.signOut()
                    isLoggedIn = false
                    navController.navigate("auth") {
                        popUpTo("home") { inclusive = true }
                    }
                    Unit
                }

                AppBar(
                    currentUser = Firebase.auth.currentUser,
                    onWithdraw = onLogout,
                    content = { paddingValues ->
                        NavHost(
                            navController = navController,
                            startDestination = if (!isLoggedIn) "auth" else "home",
                            modifier = Modifier.padding(paddingValues)
                        ) {
                            composable("home") { HomeScreen(navController) }
                            composable("write") { WriteLetterScreen(navController) }
                            composable("auth") {
                                AuthScreen(navController) {
                                    isLoggedIn = true
                                }
                            }
                            composable(
                                route = "writtenLetter?writer={writer}&documentType={documentType}&prompt={prompt}",
                                arguments = listOf(
                                    androidx.navigation.navArgument("writer") { defaultValue = "" },
                                    androidx.navigation.navArgument("documentType") { defaultValue = "" },
                                    androidx.navigation.navArgument("prompt") { defaultValue = "" }
                                )
                            ) { backStackEntry ->
                                val writer = backStackEntry.arguments?.getString("writer") ?: ""
                                val documentType = backStackEntry.arguments?.getString("documentType") ?: ""
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
                    },
                    navController = navController
                )
            }
        }
    }
}
