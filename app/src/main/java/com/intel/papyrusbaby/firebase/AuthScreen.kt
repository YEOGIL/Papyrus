package com.intel.papyrusbaby.firebase

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// 비밀번호 규칙 예시: 대문자, 소문자, 숫자, 특수문자(!@#$%^&*) 각각 최소 1개 이상, 길이 8~16자
private val PASSWORD_REGEX = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,16}$")

fun isPasswordValid(password: String): Boolean {
    return PASSWORD_REGEX.matches(password)
}

@Composable
fun AuthScreenEmailPassword(
    navController: NavController,
    onUserAuthenticated: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    // (1) 회원가입 / 로그인 모드
    var isSignUpMode by remember { mutableStateOf(true) }

    // (2) 입력 상태
    var name by remember { mutableStateOf("") }  // 회원가입용 이름
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // (3) 메시지
    var message by remember { mutableStateOf("") }
    val TAG = "EmailAuthScreen"

    // (4) 비밀번호 규칙 & 일치 여부
    val isPasswordRuleOk = isPasswordValid(password)
    val isPasswordMatch = (password == confirmPassword)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            // 아무 데나 탭하면 포커스 해제
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 모드별 타이틀
        Text(text = if (isSignUpMode) "Sign Up" else "Sign In")

        // (A) 회원가입 모드면 이름 입력
        if (isSignUpMode) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // (B) 이메일 입력
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // (C) 비밀번호 입력
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                Icon(
                    imageVector = image,
                    contentDescription = "Toggle password visibility",
                    modifier = Modifier.clickable {
                        passwordVisible = !passwordVisible
                    }
                )
            }
        )

        // (D) 회원가입 모드일 때만 비밀번호 규칙 안내 표시
        if (isSignUpMode && password.isNotEmpty()) {
            if (!isPasswordRuleOk) {
                Text(
                    text = "비밀번호는 대소문자, 숫자, 특수문자(!@#\$%^&*)를 포함해 8~16자로 입력해주세요.",
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Text(
                    text = "사용 가능한 비밀번호 형식입니다.",
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // (E) 회원가입 모드인 경우: 비번 확인란 추가
        if (isSignUpMode) {
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    Icon(
                        imageVector = image,
                        contentDescription = "Toggle confirmPassword visibility",
                        modifier = Modifier.clickable {
                            confirmPasswordVisible = !confirmPasswordVisible
                        }
                    )
                }
            )
            if (confirmPassword.isNotEmpty()) {
                if (!isPasswordMatch) {
                    Text(
                        text = "비밀번호가 일치하지 않습니다.",
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    Text(
                        text = "비밀번호가 일치합니다.",
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // (F) 회원가입 / 로그인 버튼
        Button(
            onClick = {
                if (isSignUpMode) {
                    // 회원가입
                    if (!isPasswordRuleOk) {
                        message = "비밀번호 규칙을 확인해주세요."
                        return@Button
                    }
                    if (!isPasswordMatch) {
                        message = "비밀번호가 일치하지 않습니다."
                        return@Button
                    }
                    signUpWithEmail(
                        name = name,
                        email = email,
                        password = password,
                        onSuccess = {
                            message = "회원가입 성공!"
                            Log.d(TAG, "회원가입 성공, user=${Firebase.auth.currentUser}")
                            onUserAuthenticated()
                            navController.navigate("home") {
                                popUpTo("auth") { inclusive = true }
                            }
                        },
                        onFail = { errorMsg ->
                            message = "회원가입 실패: $errorMsg"
                        }
                    )
                } else {
                    // 로그인
                    signInWithEmail(
                        email = email,
                        password = password,
                        onSuccess = {
                            message = "로그인 성공!"
                            Log.d(TAG, "로그인 성공, user=${Firebase.auth.currentUser}")
                            onUserAuthenticated()
                            navController.navigate("home") {
                                popUpTo("auth") { inclusive = true }
                            }
                        },
                        onFail = { errorMsg ->
                            message = "로그인 실패: $errorMsg"
                        }
                    )
                }
            }
        ) {
            Text(text = if (isSignUpMode) "Sign Up" else "Sign In")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // (G) 모드 전환 버튼
        Button(
            onClick = {
                isSignUpMode = !isSignUpMode
                // 입력값 초기화
                name = ""
                email = ""
                password = ""
                confirmPassword = ""
                passwordVisible = false
                confirmPasswordVisible = false
                message = ""
            }
        ) {
            Text(text = if (isSignUpMode) "Go to Sign In" else "Go to Sign Up")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 안내 메시지
        Text(text = message)
    }
}

/**
 * Firebase 이메일/비밀번호 회원가입
 */
fun signUpWithEmail(
    name: String, email: String, password: String,
    onSuccess: () -> Unit,
    onFail: (String) -> Unit
) {
    Firebase.auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // displayName 업데이트(옵션)
                val user = Firebase.auth.currentUser
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest
                    .Builder()
                    .setDisplayName(name)
                    .build()
                user?.updateProfile(profileUpdates)?.addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        onSuccess()
                    } else {
                        onFail(updateTask.exception?.message ?: "Failed to update displayName")
                    }
                }
            } else {
                onFail(task.exception?.message ?: "Sign Up Failed")
            }
        }
}

/**
 * Firebase 이메일/비밀번호 로그인
 */
fun signInWithEmail(
    email: String, password: String,
    onSuccess: () -> Unit,
    onFail: (String) -> Unit
) {
    Firebase.auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess()
            } else {
                onFail(task.exception?.message ?: "Sign In Failed")
            }
        }
}

@Preview(showBackground = true)
@Composable
fun PreviewAuthScreenEmailPassword() {
    AuthScreenEmailPassword(
        navController = rememberNavController(),
        onUserAuthenticated = {}
    )
}
