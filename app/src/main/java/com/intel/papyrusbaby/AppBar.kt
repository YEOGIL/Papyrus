package com.intel.papyrusbaby

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    currentUser: FirebaseUser?,
    onWithdraw: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
    navController: NavController
) {
    // DrawerState to control the drawer's open/close state
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    // CoroutineScope to launch coroutine actions
    val coroutineScope = rememberCoroutineScope()

    // The ModalNavigationDrawer composable
    ModalNavigationDrawer(
        drawerState = drawerState,
        // todo: 닫힘 버튼을 통해서만 drawer 닫을 수 있게 gestured disabled
        gesturesEnabled = false,
        drawerContent = {
            //Drawer 내에서 닫힘 버튼을 구현하기 위해 parameter로 coroutineScope와 drawerState 전달
            DrawerContent(navController, coroutineScope, drawerState, currentUser, onWithdraw)
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
                                    // Open the Drawer
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
                    // 현재 네비게이션 스택에서 route를 가져옴
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFfffae6))
                            .padding(horizontal = 25.dp, vertical = 10.dp)
                            .navigationBarsPadding(),
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
                            modifier = Modifier.clickable {
                                navController.navigate("home") {
                                    launchSingleTop = true
                                }
                            }
                        )

                        Icon(
                            painter = painterResource(
                                id = if (currentRoute == "write")
                                    R.drawable.icon_add_filled
                                else
                                    R.drawable.icon_add_outline
                            ),
                            tint = Color.Unspecified,
                            contentDescription = "CreateLetter",
                            modifier = Modifier.clickable {
                                navController.navigate("write") {
                                    launchSingleTop = true
                                }
                            }
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
                            modifier = Modifier.clickable {
                                navController.navigate("archive") {
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
    Column(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .fillMaxHeight()
            .background(Color(0xFFF7ECCD))
    ) {
        Icon(
            painter = painterResource(R.drawable.icon_drawerclose),
            tint = Color.Unspecified,
            contentDescription = "CloseDrawer",
            modifier = Modifier
                .padding(top = 15.dp, start = 15.dp)
                .clickable {
                    // Open the Drawer
                    coroutineScope.launch {
                        drawerState.close()
                    }
                }
        )
        Icon(
            painter = painterResource(R.drawable.papyruslogo),
            contentDescription = "Menu",
            tint = Color.Unspecified,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // 유저 정보가 있을 경우 인사말 표시
        if (currentUser != null) {
            Text(
                text = "반갑습니다, ${currentUser.displayName ?: "User"} 님",
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Text(
            text = "Menu Item 1",
            modifier = Modifier
                .padding(16.dp)
                .clickable {
                    navController.navigate("home")
                }
        )
        Text(
            text = "Menu Item 2",
            modifier = Modifier
                .padding(16.dp)
                .clickable {
                    navController.navigate("write")
                }
        )
        // Add more items as needed
        // 구글 로그 아웃
        if (currentUser != null) {
            Text(
                text = "로그 아웃",
                modifier = Modifier
                    .padding(16.dp)
                    .clickable { onWithdraw() }
            )
        }
    }
}
