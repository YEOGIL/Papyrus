package com.intel.papyrusbaby

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.intel.papyrusbaby.navigation.Screen
import com.intel.papyrusbaby.util.LogOutDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    currentUser: FirebaseUser?,
    onDeleteAccount: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
    navController: NavController
) {
    // 상단/하단바를 숨길 라우트를 정의합니다.
    // 예: 인증 화면("auth")에서는 두 바 모두 숨기고, 이미지 생성 화면("imageGeneration")에서는 하단바만 숨김
    val hideTopBarRoutes = listOf(Screen.Auth.route)
    val hideBottomBarRoutes = listOf(Screen.Auth.route, Screen.ImageGeneration.route)

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // 현재 라우트를 읽어옵니다.
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 만약 현재 라우트가 hideTopBarRoutes에 포함되어 있다면 상단바와 하단바를 모두 숨기고 content만 노출합니다.
    if (currentRoute in hideTopBarRoutes) {
        content(PaddingValues())
        return
    }

    // 그 외의 경우 ModalNavigationDrawer와 Scaffold로 상단바와 하단바를 구성합니다.
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            DrawerContent(
                navController = navController,
                coroutineScope = coroutineScope,
                drawerState = drawerState,
                currentUser = currentUser,
                onDeleteAccount = onDeleteAccount
            )
        },
        content = {
            Scaffold(
                topBar = {
                    // 상단바는 hideTopBarRoutes에 포함되지 않은 경우 항상 노출합니다.
                    PapyrusTopBar(
                        navController = navController,
                        currentRoute = currentRoute,
                        onDrawerOpen = { coroutineScope.launch { drawerState.open() } }
                    )
                },
                bottomBar = {
                    // 하단바는 현재 라우트가 hideBottomBarRoutes에 포함되어 있지 않은 경우에만 노출합니다.
                    if (currentRoute !in hideBottomBarRoutes) {
                        PapyrusBottomBar(
                            currentRoute = currentRoute,
                            onHomeClick = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onWriteClick = {
                                navController.navigate(Screen.Write.createRoute(writer = "")) {
                                    popUpTo(Screen.Write.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onArchiveClick = {
                                navController.navigate(Screen.Archive.route) {
                                    popUpTo(Screen.Archive.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            ) { paddingValues -> content(paddingValues) }
        }
    )
}

// Drawer content 정의
@Composable
fun DrawerContent(
    navController: NavController,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    drawerState: DrawerState,
    currentUser: FirebaseUser?,
    onDeleteAccount: () -> Unit
) {
    var showLogOutDialog by remember { mutableStateOf(false) }

    if (showLogOutDialog) {
        LogOutDialog(
            onDismiss = { showLogOutDialog = false },
            onLogOut = {
                // signOut
                Firebase.auth.signOut()
                coroutineScope.launch { drawerState.close() }
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
                    .clickable { coroutineScope.launch { drawerState.close() } }
            )
            Icon(
                painter = painterResource(R.drawable.papyruslogo),
                contentDescription = "Menu",
                tint = Color.Unspecified,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (currentUser != null) {
            Text(
                text = "반갑습니다, ${currentUser.displayName ?: currentUser.email ?: "User"} 님",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        } else {
            Text(
                text = "로그인이 필요합니다",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
            Text(text = "홈으로", modifier = Modifier.padding(16.dp))
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
            Text(text = "작성하기", modifier = Modifier.padding(16.dp))
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
            Text(text = "보관함", modifier = Modifier.padding(16.dp))
        }
        if (currentUser != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLogOutDialog = true }
            ) {
                Text(text = "로그아웃", modifier = Modifier.padding(16.dp))
            }

            // 회원 탈퇴
//        Text(
//            text = "회원 탈퇴",
//            modifier = Modifier
//                .padding(16.dp)
//                .clickable {
//                    // 회원 탈퇴 로직
//                    // user.delete() -> signOut 순서
//                    onDeleteAccount()
//                    coroutineScope.launch { drawerState.close() }
//                }
//        )
        }
    }
}

@Composable
fun PapyrusTopBar(
    navController: NavController,
    currentRoute: String?,
    onDrawerOpen: () -> Unit
) {
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
                .padding(12.dp)
                .clickable { onDrawerOpen() }
        )

        // Title
        Icon(
            painter = painterResource(R.drawable.papyruslogo),
            contentDescription = "Menu",
            tint = Color.Unspecified,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(12.dp)
        )

        // Back button
        if (currentRoute == "archiveDetail" || currentRoute == "imageGeneration") {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(12.dp)
            ) {
                Text(
                    text = "뒤로가기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF5C5945),
                    modifier = Modifier
                        .clickable { navController.popBackStack() }
                        .border(1.dp, shape = RoundedCornerShape(5.dp), color = Color(0xFF94907F))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
fun PapyrusBottomBar(
    currentRoute: String?,
    onHomeClick: () -> Unit,
    onWriteClick: () -> Unit,
    onArchiveClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFfffae6))
            .padding(horizontal = 25.dp, vertical = 10.dp)
            .navigationBarsPadding()
    ) {
        Icon(
            painter = painterResource(
                id = if (currentRoute == "home")
                    R.drawable.icon_home_filled
                else
                    R.drawable.icon_home_outline
            ),
            tint = Color.Unspecified,
            contentDescription = "Home",
            modifier = Modifier.clickable { onHomeClick() }
        )
        Icon(
            painter = painterResource(
                id = if (currentRoute == "write?writer={writer}")
                    R.drawable.icon_add_filled
                else
                    R.drawable.icon_add_outline
            ),
            tint = Color.Unspecified,
            contentDescription = "CreateLetter",
            modifier = Modifier.clickable { onWriteClick() }
        )
        Icon(
            painter = painterResource(
                id = if (currentRoute == "archive")
                    R.drawable.icon_archive_filled
                else
                    R.drawable.icon_archive_outline
            ),
            tint = Color.Unspecified,
            contentDescription = "ArchivedLetters",
            modifier = Modifier.clickable { onArchiveClick() }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppBarPreview() {
    val navController = NavController(context = LocalContext.current)
    AppBar(
        currentUser = null,
        onDeleteAccount = {},
        content = { PaddingValues() },
        navController = navController
    )
}