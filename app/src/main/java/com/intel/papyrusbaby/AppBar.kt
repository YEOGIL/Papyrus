package com.intel.papyrusbaby

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.intel.papyrusbaby.util.LogOutDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    currentUser: FirebaseUser?,
    onWithdraw: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
    navController: NavController
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // 현재 라우트를 가져옴
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 만약 currentRoute가 "auth"라면, 상단바/하단바 없이 content만 보여주기
    // -> 또는 그냥 ModalNavigationDrawer 자체를 생략하는 방법도 있음
    if (currentRoute == "auth") {
        // top/bottom bar 숨김: 바로 content만 표시
        content(PaddingValues())
        return
    }

    // 그 외 화면일 때만 Drawer + topBar + bottomBar
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            DrawerContent(
                navController = navController,
                coroutineScope = coroutineScope,
                drawerState = drawerState,
                currentUser = currentUser,
                onWithdraw = onWithdraw
            )
        },
        content = {
            Scaffold(
                topBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFfffae6))
                            .statusBarsPadding()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.icon_draweropen),
                            contentDescription = "drawerOpen",
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(top = 15.dp, start = 15.dp)
                                .clickable {
                                    coroutineScope.launch {
                                        drawerState.open()
                                    }
                                }
                        )

                        // Title
                        Icon(
                            painter = painterResource(R.drawable.papyruslogo),
                            contentDescription = "Menu",
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(top = 20.dp)
                        )
                    }
                },
                bottomBar = {
                    // bottom bar
                    val currentRouteInner = currentRoute
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFfffae6))
                            .padding(horizontal = 25.dp, vertical = 10.dp)
                            .navigationBarsPadding()
                    ) {
                        // Home icon
                        Icon(
                            painter = painterResource(
                                id = if (currentRouteInner == "home")
                                    R.drawable.icon_home_filled
                                else
                                    R.drawable.icon_home_outline
                            ),
                            tint = Color.Unspecified,
                            contentDescription = "Home",
                            modifier = Modifier.clickable {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                        // Write icon
                        Icon(
                            painter = painterResource(
                                id = if (currentRouteInner == "write")
                                    R.drawable.icon_add_filled
                                else
                                    R.drawable.icon_add_outline
                            ),
                            tint = Color.Unspecified,
                            contentDescription = "CreateLetter",
                            modifier = Modifier.clickable {
                                navController.navigate("write") {
                                    popUpTo("write") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                        // Archive icon
                        Icon(
                            painter = painterResource(
                                id = if (currentRouteInner == "archive")
                                    R.drawable.icon_archive_filled
                                else
                                    R.drawable.icon_archive_outline
                            ),
                            tint = Color.Unspecified,
                            contentDescription = "ArchivedLetters",
                            modifier = Modifier.clickable {
                                navController.navigate("archive") {
                                    popUpTo("archive") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            ) { paddingValues ->
                content(paddingValues)
            }
        }
    )
}

// Drawer content definition
@Composable
fun DrawerContent(
    navController: NavController,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    drawerState: DrawerState,
    currentUser: FirebaseUser?,
    onWithdraw: () -> Unit
) {
    var showLogOutDialog by remember { mutableStateOf(false) }

    if (showLogOutDialog) {
        LogOutDialog(
            onDismiss = { showLogOutDialog = false },
            onLogOut = {
                // signOut
                Firebase.auth.signOut()
                coroutineScope.launch { drawerState.close() }
                // 그 후 네비게이트
                navController.navigate("auth") {
                    popUpTo("auth") { inclusive = true }
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .fillMaxHeight()
            .background(Color(0xFFF7ECCD))
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                painter = painterResource(R.drawable.icon_drawerclose),
                tint = Color.Unspecified,
                contentDescription = "CloseDrawer",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable {
                        // Close the Drawer
                        coroutineScope.launch { drawerState.close() }
                    }
            )
            Icon(
                painter = painterResource(R.drawable.papyruslogo),
                contentDescription = "Menu",
                tint = Color.Unspecified,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 유저 정보가 있을 경우 인사말 표시
        if (currentUser != null) {
            Text(
                text = "반갑습니다, ${currentUser.displayName ?: currentUser.email ?: "User"} 님",
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        } else {
            Text(
                text = "로그인이 필요합니다",
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    coroutineScope.launch { drawerState.close() }
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                        launchSingleTop = true
                    }
                }
        ) {
            Text(
                text = "홈으로",
                modifier = Modifier.padding(16.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    coroutineScope.launch { drawerState.close() }
                    navController.navigate("write") {
                        popUpTo("write") { inclusive = true }
                        launchSingleTop = true
                    }
                }
        ) {
            Text(
                text = "작성하기",
                modifier = Modifier.padding(16.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    coroutineScope.launch { drawerState.close() }
                    navController.navigate("archive") {
                        popUpTo("archive") { inclusive = true }
                        launchSingleTop = true
                    }
                }
        ) {
            Text(
                text = "보관함",
                modifier = Modifier.padding(16.dp)
            )
        }

        // Add more items as needed

        // 로그 아웃
        if (currentUser != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLogOutDialog = true }
            ) {
                Text(
                    text = "로그아웃",
                    modifier = Modifier.padding(16.dp)
                )
            }

            // 회원 탈퇴
//        Text(
//            text = "회원 탈퇴",
//            modifier = Modifier
//                .padding(16.dp)
//                .clickable {
//                    // 회원 탈퇴 로직
//                    // user.delete() -> signOut 순서
//                    onWithdraw()
//                    coroutineScope.launch { drawerState.close() }
//                }
//        )
        }
    }
}
