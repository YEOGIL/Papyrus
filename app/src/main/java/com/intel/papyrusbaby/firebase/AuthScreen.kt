package com.intel.papyrusbaby.firebase

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.intel.papyrusbaby.R
import com.intel.papyrusbaby.util.ExitDialog

private val PASSWORD_REGEX = Regex("^(?=.*[A-Za-z])(?=.*\\d).{8,16}$")

fun isPasswordValid(password: String): Boolean {
    return PASSWORD_REGEX.matches(password)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreenEmailPassword(
    navController: NavController,
    onUserAuthenticated: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // 뒤로가기 다이얼로그
    var showExitDialog by remember { mutableStateOf(false) }
    BackHandler { showExitDialog = true }
    if (showExitDialog) {
        ExitDialog(
            onDismiss = { showExitDialog = false },
            activity = activity
        )
    }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

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
            .background(Color(0xFFfffae6))
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            // 화면의 빈 공간을 탭하면 키보드 포커스 해제
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단 영역: 아이콘 위치 (회원가입/로그인 동일)
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(250.dp)
        )

        // (A) 회원가입 모드면 이름 입력
        if (isSignUpMode) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("이름") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // (B) 이메일 입력
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        // (C) 비밀번호 입력
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = if (isSignUpMode) ImeAction.Next else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                onDone = { focusManager.clearFocus() }
            ),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image =
                    if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                Icon(
                    imageVector = image,
                    contentDescription = "Toggle password visibility",
                    modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                )
            }
        )
        if (isSignUpMode) {
            Text(
                text = "비밀번호는 영문자와 숫자를 포함해 8~16자로 입력해주세요.",
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // (D) 회원가입 모드일 때 비밀번호 규칙 안내 표시
        if (isSignUpMode && password.isNotEmpty()) {
            if (!isPasswordRuleOk) {
                Text(
                    text = "사용할 수 없는 비밀번호 형식입니다.",
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

        // (E) 회원가입 모드인 경우: 비밀번호 재확인
        if (isSignUpMode) {
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("비밀번호 재확인") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image =
                        if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    Icon(
                        imageVector = image,
                        contentDescription = "Toggle confirmPassword visibility",
                        modifier = Modifier.clickable {
                            confirmPasswordVisible = !confirmPasswordVisible
                        }
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF35713E))
        ) {
            Text(text = if (isSignUpMode) "회원가입" else "로그인")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // (G) 모드 전환 텍스트 (회원가입 <-> 로그인)
        Text(
            text = if (isSignUpMode) "로그인으로 가기" else "회원가입",
            style = TextStyle(
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.clickable {
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
        )
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
    email: String,
    password: String,
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
