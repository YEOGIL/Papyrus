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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(content: @Composable (PaddingValues) -> Unit, navController: NavController) {
    // DrawerState to control the drawer's open/close state
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    // CoroutineScope to launch coroutine actions
    val coroutineScope = rememberCoroutineScope()

    // The ModalNavigationDrawer composable
    ModalNavigationDrawer(
        drawerState = drawerState,
        //닫힘 버튼을 통해서만 drawer 닫을 수 있게 gestured disabled
        gesturesEnabled = false,
        drawerContent = {
            //Drawer 내에서 닫힘 버튼을 구현하기 위해 parameter로 coroutineScope와 drawerState 전달
            DrawerContent(navController, coroutineScope, drawerState)
        },
        content = {
            Scaffold(
                topBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .padding(horizontal = 10.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.icon_draweropen),
                            contentDescription = "drawerOpen",
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
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
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                },
                bottomBar = {
                    // 현재 네비게이션 스택에서 route를 가져옴
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 25.dp, vertical = 10.dp),
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
    coroutineScope: CoroutineScope,
    drawerState: DrawerState
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
            modifier = Modifier.clickable {
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
    }
}
