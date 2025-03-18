package com.intel.papyrusbaby.firebase

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.intel.papyrusbaby.R

private const val TAG = "GoogleLoginDebug"

@Composable
fun AuthScreen(navController: NavController, onUserAuthenticated: () -> Unit) {
    var message by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Google로 로그인", modifier = Modifier.padding(bottom = 16.dp))
        GoogleSignInButton { success ->
            if (success) {
                Log.d(TAG, "Firebase 인증 성공 - 사용자 UID: ${Firebase.auth.currentUser?.uid}")
                if (Firebase.auth.currentUser != null) {
                    message = "Google 로그인 성공!"
                    onUserAuthenticated()
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                } else {
                    Log.e(TAG, "Firebase 인증 성공 후에도 사용자 정보가 없음")
                    message = "Firebase 인증 성공 후 사용자 정보 없음"
                }
            } else {
                Log.e(TAG, "Google 로그인 또는 Firebase 인증 실패")
                message = "Google 로그인 실패"
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message)
    }
}

@Composable
fun GoogleSignInButton(onResult: (Boolean) -> Unit) {
    val context = LocalContext.current

    // [1] GoogleSignInOptions 생성: 공식 문서의 가이드에 따라 ID 토큰과 이메일 요청
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id)) // Firebase 콘솔의 웹 클라이언트 ID 사용
        .requestEmail()
        .build()
    Log.d(TAG, "GoogleSignInOptions 생성 완료: $gso")

    // [2] GoogleSignInClient 생성
    val googleSignInClient = GoogleSignIn.getClient(context, gso)
    Log.d(TAG, "GoogleSignInClient 생성 완료")

    // [3] ActivityResultLauncher 등록: 구글 로그인 인텐트 결과 수신
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "Google SignIn Activity 결과: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            // [4] 인텐트에서 Google 계정 정보 추출
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                Log.d(TAG, "Google 계정 정보 획득 성공, 이메일: ${account?.email}")

                if (account == null) {
                    Log.e(TAG, "Google 계정 정보가 null입니다.")
                    onResult(false)
                    return@rememberLauncherForActivityResult
                }

                val idToken = account.idToken
                Log.d(TAG, "받은 ID 토큰: $idToken")

                // [5] Google ID 토큰으로 Firebase 인증 Credential 생성
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                Log.d(TAG, "Firebase 인증 Credential 생성 완료")

                // [6] Firebase로 로그인 시도
                Firebase.auth.signInWithCredential(credential)
                    .addOnCompleteListener { authResult ->
                        if (authResult.isSuccessful) {
                            Log.d(TAG, "Firebase 인증 성공, 사용자 UID: ${Firebase.auth.currentUser?.uid}")
                        } else {
                            Log.e(TAG, "Firebase 인증 실패", authResult.exception)
                        }
                        onResult(authResult.isSuccessful)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Google 로그인 중 예외 발생", e)
                onResult(false)
            }
        } else {
            Log.e(TAG, "Google 로그인 취소되었거나 실패, 결과 코드: ${result.resultCode}")
            onResult(false)
        }
    }

    // [7] 버튼 클릭 시 구글 로그인 인텐트 실행
    Button(onClick = {
        Log.d(TAG, "GoogleSignInButton 클릭 - 로그인 프로세스 시작")
        launcher.launch(googleSignInClient.signInIntent)
    }) {
        Text("Google로 로그인")
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    AuthScreen(navController = rememberNavController()) {}
}
