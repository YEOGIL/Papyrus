package com.intel.papyrusbaby

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.intel.papyrusbaby.firebase.AuthScreenEmailPassword
import com.intel.papyrusbaby.screen.ArchivedLetterScreen
import com.intel.papyrusbaby.screen.HomeScreen
import com.intel.papyrusbaby.screen.WriteLetterScreen
import com.intel.papyrusbaby.screen.WrittenLetterScreen
import com.intel.papyrusbaby.ui.theme.PapyrusBabyTheme

class MainActivity : ComponentActivity() {
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // (1) Firebase Auth의 currentUser를 State로 관리
        val currentFirebaseUser = mutableStateOf(Firebase.auth.currentUser)

        // (2) AuthStateListener 등록
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            currentFirebaseUser.value = auth.currentUser
        }
        // (3) onCreate 시점에 리스너 추가, onDestroy 시점에 제거
        Firebase.auth.addAuthStateListener(authStateListener)

        setContent {
            PapyrusBabyTheme {
                val navController = rememberNavController()

                // 회원 탈퇴 콜백
                val onDeleteAccount: () -> Unit = {
                    // user.delete() → signOut 순서
//                    currentFirebaseUser.value?.delete()?.addOnCompleteListener {
//                        Firebase.auth.signOut()
//                        // navigate to auth
//                        navController.navigate("auth") {
//                            popUpTo("home") { inclusive = true }
//                        }
//                    }
                }

                AppBar(
                    // currentUser를 state에서 읽어옴
                    currentUser = currentFirebaseUser.value,
                    onDeleteAccount = onDeleteAccount,
                    content = { paddingValues ->
                        NavHost(
                            navController = navController,
                            startDestination = if (currentFirebaseUser.value == null) "auth" else "home",
                            modifier = Modifier.padding(paddingValues)
                        ) {
                            composable("auth") {
                                AuthScreenEmailPassword(navController) {
                                    // 로그인/회원가입 성공 시 호출
                                    // 여기서 currentUser.value가 갱신되면 곧바로 Drawer 문구도 바뀜
                                    navController.navigate("home") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                }
                            }
                            composable("home") { HomeScreen(navController) }
                            composable("write") { WriteLetterScreen(navController) }
                            composable("archive") { ArchivedLetterScreen(navController) }
                            composable(
                                route = "writtenLetter?writer={writer}&documentType={documentType}&prompt={prompt}",
                                arguments = listOf(
                                    androidx.navigation.navArgument("writer") { defaultValue = "" },
                                    androidx.navigation.navArgument("documentType") {
                                        defaultValue = ""
                                    },
                                    androidx.navigation.navArgument("prompt") { defaultValue = "" }
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
                        }
                    },
                    navController = navController
                )
            }
        }
    }

    override fun onDestroy() {
        // (4) 라이프사이클에 맞춰 리스너 제거
        Firebase.auth.removeAuthStateListener(authStateListener)
        super.onDestroy()
    }
}
