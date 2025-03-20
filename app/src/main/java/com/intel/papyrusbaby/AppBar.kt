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

// 공통 색상 상수
private val BackgroundColor = Color(0xFFfffae6)
private val DrawerBackgroundColor = Color(0xFFF7ECCD)
private val TextColor = Color(0xFF5C5945)
private val BorderColor = Color(0xFF94907F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    currentUser: FirebaseUser?,
    onDeleteAccount: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
    navController: NavController
) {
    // 상단/하단바를 숨길 라우트 목록
    val hideTopBarRoutes = listOf(Screen.Auth.route)
    val hideBottomBarRoutes = listOf(Screen.Auth.route, Screen.ImageGeneration.route)

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // 현재 라우트를 읽어옵니다.
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 상단바를 숨길 경우 content만 노출
    if (currentRoute in hideTopBarRoutes) {
        content(PaddingValues())
        return
    }

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
                    PapyrusTopBar(
                        navController = navController,
                        currentRoute = currentRoute,
                        onDrawerOpen = { coroutineScope.launch { drawerState.open() } }
                    )
                },
                bottomBar = {
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
            ) { paddingValues ->
                content(paddingValues)
            }
        }
    )
}

// Drawer에 사용되는 단일 항목 컴포저블 (텍스트와 클릭 이벤트 처리)
@Composable
fun DrawerNavItem(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Text(text = text, modifier = Modifier.padding(16.dp))
    }
}

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
            .background(DrawerBackgroundColor)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // 상단 Drawer 헤더 (닫기 아이콘과 로고)
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                painter = painterResource(R.drawable.icon_drawerclose),
                tint = Color.Unspecified,
                contentDescription = "Close Drawer",
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
        // 사용자 인사말 또는 로그인 안내
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
        // 네비게이션 항목들
        DrawerNavItem(text = "홈으로") {
            coroutineScope.launch { drawerState.close() }
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
                launchSingleTop = true
            }
        }
        DrawerNavItem(text = "작성하기") {
            coroutineScope.launch { drawerState.close() }
            navController.navigate(Screen.Write.route) {
                popUpTo(Screen.Write.route) { inclusive = true }
                launchSingleTop = true
            }
        }
        DrawerNavItem(text = "보관함") {
            coroutineScope.launch { drawerState.close() }
            navController.navigate(Screen.Archive.route) {
                popUpTo(Screen.Archive.route) { inclusive = true }
                launchSingleTop = true
            }
        }
        if (currentUser != null) {
            DrawerNavItem(text = "로그아웃") {
                showLogOutDialog = true
            }
            // 회원 탈퇴 관련 코드는 필요에 따라 주석 해제하여 추가
            /*
            DrawerNavItem(text = "회원 탈퇴") {
                onDeleteAccount()
                coroutineScope.launch { drawerState.close() }
            }
            */
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
            .background(BackgroundColor)
            .statusBarsPadding()
    ) {
        Icon(
            painter = painterResource(R.drawable.icon_draweropen),
            contentDescription = "Open Drawer",
            tint = Color.Unspecified,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(top = 12.dp, start = 12.dp, end = 12.dp)
                .clickable { onDrawerOpen() }
        )

        // 중앙 로고
        Icon(
            painter = painterResource(R.drawable.papyruslogo),
            contentDescription = "Menu Logo",
            tint = Color.Unspecified,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 12.dp)
        )

        // 특정 라우트에서 뒤로가기 버튼 표시
        if (currentRoute == Screen.ArchiveDetail.route || currentRoute == Screen.ImageGeneration.route) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(top = 12.dp, end = 12.dp)
            ) {
                Text(
                    text = "뒤로가기",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextColor,
                    modifier = Modifier
                        .clickable { navController.popBackStack() }
                        .border(1.dp, BorderColor, shape = RoundedCornerShape(5.dp))
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
            .background(BackgroundColor)
            .padding(horizontal = 25.dp, vertical = 10.dp)
            .navigationBarsPadding()
    ) {
        BottomBarIcon(
            currentRoute = currentRoute,
            targetRoute = Screen.Home.route,
            filledIconRes = R.drawable.icon_home_filled,
            outlineIconRes = R.drawable.icon_home_outline,
            contentDescription = "Home",
            onClick = onHomeClick
        )
        BottomBarIcon(
            currentRoute = currentRoute,
            targetRoute = Screen.Write.route,
            filledIconRes = R.drawable.icon_add_filled,
            outlineIconRes = R.drawable.icon_add_outline,
            contentDescription = "Create Letter",
            onClick = onWriteClick
        )
        BottomBarIcon(
            currentRoute = currentRoute,
            targetRoute = Screen.Archive.route,
            filledIconRes = R.drawable.icon_archive_filled,
            outlineIconRes = R.drawable.icon_archive_outline,
            contentDescription = "Archived Letters",
            onClick = onArchiveClick
        )
    }
}

// 공통 BottomBar 아이콘 컴포저블: 현재 라우트와 비교하여 채워진 아이콘 혹은 외곽 아이콘을 보여줍니다.
@Composable
fun BottomBarIcon(
    currentRoute: String?,
    targetRoute: String,
    filledIconRes: Int,
    outlineIconRes: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    val iconRes = if (currentRoute == targetRoute) filledIconRes else outlineIconRes
    Icon(
        painter = painterResource(id = iconRes),
        tint = Color.Unspecified,
        contentDescription = contentDescription,
        modifier = Modifier.clickable { onClick() }
    )
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
