package com.intel.papyrusbaby.firebase

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.intel.papyrusbaby.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class User(
    val id: String,
    val name: String,
    val email: String,
    val timestamp: String,
    val deleted: Boolean = false
)

fun getCurrentTimestamp(): String {
    // "yyyy-MM-dd_HH-mm-ss" 형식으로 현재 시간 포맷팅
    val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    return sdf.format(Date())
}

@Composable
fun AuthScreen(navController: NavController, onUserAuthenticated: (User) -> Unit) {
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
                val user = Firebase.auth.currentUser
                if (user != null) {
                    val displayName = user.displayName ?: "NoName"
                    val email = user.email ?: ""
                    val newUser = User(
                        id = user.uid,
                        name = displayName,
                        email = email,
                        timestamp = getCurrentTimestamp(),
                        deleted = false
                    )
                    message = "Google 로그인 성공!"
                    onUserAuthenticated(newUser)
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            } else {
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
    // ActivityResultLauncher 생성 (StartActivityForResult)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                Firebase.auth.signInWithCredential(credential)
                    .addOnCompleteListener { authResult ->
                        onResult(authResult.isSuccessful)
                    }
            } catch (e: Exception) {
                onResult(false)
            }
        } else {
            onResult(false)
        }
    }

    Button(onClick = {
        // GoogleSignInOptions 구성 (default_web_client_id는 strings.xml에 등록되어 있어야 함)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        launcher.launch(googleSignInClient.signInIntent)
    }) {
        Text("Google로 로그인")
    }
}