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
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.intel.papyrusbaby.firebase.AuthScreenEmailPassword
import com.intel.papyrusbaby.screen.ArchivedListContentsScreen
import com.intel.papyrusbaby.screen.ArchivedListScreen
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

                // writtenLetter 화면에 필요한 네비게이션 인자들을 미리 정의
                val writtenLetterArgs = listOf(
                    navArgument("writer") { defaultValue = "" },
                    navArgument("documentType") { defaultValue = "" },
                    navArgument("prompt") { defaultValue = "" },
                    navArgument("theme") { defaultValue = "" }
                )

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
                                    navController.navigate("home") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                }
                            }
                            composable("home") { HomeScreen(navController) }
                            composable(
                                route = "write?writer={writer}",
                                arguments = listOf(
                                    navArgument("writer") { defaultValue = "" }
                                )
                            ) { backStackEntry ->
                                val writerParam = backStackEntry.arguments?.getString("writer") ?: ""
                                WriteLetterScreen(navController, writerParam)
                            }
                            composable(
                                route = "writtenLetter?writer={writer}&documentType={documentType}&prompt={prompt}&theme={theme}",
                                arguments = writtenLetterArgs
                            ) { backStackEntry ->
                                val writer = backStackEntry.arguments?.getString("writer") ?: ""
                                val documentType = backStackEntry.arguments?.getString("documentType") ?: ""
                                val prompt = backStackEntry.arguments?.getString("prompt") ?: ""
                                val theme = backStackEntry.arguments?.getString("theme") ?: ""

                                WrittenLetterScreen(
                                    writer = writer,
                                    documentType = documentType,
                                    prompt = prompt,
                                    theme = theme,  // 전달받은 테마 문자열
                                    navController = navController
                                )
                            }
                            composable("archive") { ArchivedListScreen(navController) }
                            composable(
                                route = "archiveDetail/{docId}",
                                arguments = listOf(
                                    navArgument("docId") { defaultValue = "" }
                                )
                            ) { backStackEntry ->
                                val docId = backStackEntry.arguments?.getString("docId") ?: ""
                                ArchivedListContentsScreen(docId = docId, navController = navController)
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
