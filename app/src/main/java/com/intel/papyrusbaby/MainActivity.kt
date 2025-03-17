package com.intel.papyrusbaby

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.navigation.navArgument
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.intel.papyrusbaby.firebase.AuthScreen
import com.intel.papyrusbaby.firebase.User
import com.intel.papyrusbaby.screen.ArchivedLetterScreen
import com.intel.papyrusbaby.screen.HomeScreen
import com.intel.papyrusbaby.screen.WriteLetterScreen
import com.intel.papyrusbaby.screen.WrittenLetterScreen
import com.intel.papyrusbaby.ui.theme.PapyrusBabyTheme

class MainActivity : ComponentActivity() {
    // currentUser 상태를 최상위에서 관리 (실제로는 ViewModel 등으로 관리하는 것이 좋습니다)
    private val db = Firebase.firestore
    private val PREFS_NAME = "UserPrefs"
    private val KEY_USER_ID = "userId"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PapyrusBabyTheme {
                val navController = rememberNavController()
                var currentUser by remember { mutableStateOf<User?>(null) }
                // SharedPreferences에서 저장된 유저 ID 불러오기
                val sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val storedUserId = sharedPref.getString(KEY_USER_ID, null)

                // storedUserId가 있다면 Firestore에서 유저 정보 조회
                LaunchedEffect(storedUserId) {
                    storedUserId?.let { userId ->
                        db.collection("users").document(userId).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val data = document.data
                                    if (data != null && (data["deleted"] as? Boolean == false)) {
                                        val user = User(
                                            id = data["id"] as? String ?: "",
                                            name = data["name"] as? String ?: "",
                                            email = data["email"] as? String ?: "",
                                            timestamp = data["timestamp"] as? String ?: "",
                                            deleted = data["deleted"] as? Boolean ?: false
                                        )
                                        currentUser = user
                                    }
                                }
                            }
                    }
                }

                // 회원 탈퇴 콜백: DB 업데이트 후 currentUser를 null로, SharedPreferences에서도 삭제
                val onWithdraw: () -> Unit = {
                    currentUser?.let { user ->
                        db.collection("users").document(user.id)
                            .update("deleted", true)
                            .addOnSuccessListener {
                                currentUser = null
                                sharedPref.edit().remove(KEY_USER_ID).apply()
                                navController.navigate("auth") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                    }
                    Unit
                }

                // AppBar를 최상위로 사용하고, content 람다 내부에 NavHost를 배치합니다.
                AppBar(
                    currentUser = currentUser,
                    onWithdraw = onWithdraw,
                    content = { paddingValues ->
                        NavHost(
                            navController = navController,
                            // 초기에는 currentUser가 null이면 "auth" 화면, 아니면 "home" 화면으로 시작
                            startDestination = if (currentUser == null) "auth" else "home",
                            modifier = Modifier.padding(paddingValues)
                        ) {
                            composable("home") { HomeScreen(navController) }
                            composable("write") { WriteLetterScreen(navController) }
                            composable("auth") {
                                // 회원가입 후 onUserCreated 콜백을 통해 currentUser 업데이트
                                AuthScreen(navController) { user ->
                                    currentUser = user
                                }
                            }
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
                    },
                    navController = navController
                )
            }
        }
    }
}
