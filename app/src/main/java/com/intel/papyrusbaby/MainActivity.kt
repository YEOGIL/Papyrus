package com.intel.papyrusbaby

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.intel.papyrusbaby.navigation.AppNavHost
import com.intel.papyrusbaby.navigation.Screen
import com.intel.papyrusbaby.ui.theme.PapyrusBabyTheme

class MainActivity : ComponentActivity() {
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // (1) Firebase Auth의 currentUser를 State로 관리
        val currentFirebaseUser = mutableStateOf(Firebase.auth.currentUser)

        // (2) 클래스 프로퍼티에 직접 AuthStateListener 할당
        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            currentFirebaseUser.value = auth.currentUser
        }
        // (3) 리스너 추가
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
                    currentUser = currentFirebaseUser.value,
                    onDeleteAccount = onDeleteAccount,
                    navController = navController,
                    content = { paddingValues ->
                        AppNavHost(
                            navController = navController,
                            startDestination = if (currentFirebaseUser.value == null) Screen.Auth.route else Screen.Home.route,
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        // (4) authStateListener가 초기화되었는지 확인 후 제거
        if (::authStateListener.isInitialized) {
            Firebase.auth.removeAuthStateListener(authStateListener)
        }
        super.onDestroy()
    }
}
