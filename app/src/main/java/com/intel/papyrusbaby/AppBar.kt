package com.intel.papyrusbaby

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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
        drawerContent = {
            DrawerContent(navController)
        },
        content = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        modifier = Modifier.padding(end = 7.dp, top = 10.dp),
                        title = {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                // IconButton to open the Drawer
                                IconButton(onClick = {
                                    // Open the Drawer
                                    coroutineScope.launch {
                                        drawerState.open()
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Menu",
                                        modifier = Modifier.align(Alignment.CenterStart)
                                    )
                                }

                                // Title
                                Text(
                                    text = "Papyrus",
                                    color = Color.Black,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.align(Alignment.Center)
                                )

                                // Divider
                                Divider(
                                    color = Color.Gray,
                                    thickness = 1.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                        .align(Alignment.BottomStart)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFFFFFAF3)
                        )
                    )
                },
                bottomBar = {
                    BottomAppBar(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color(0xFFFFFAF3),
                        actions = {
                            IconButton(
                                onClick = {
                                    navController.navigate("home") {
                                        launchSingleTop = true
                                        popUpTo("home")
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Home, contentDescription = "Home")
                            }
                            IconButton(
                                onClick = {
                                    navController.navigate("write") {
                                        launchSingleTop = true
                                        popUpTo("home")
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.AddCircle, contentDescription = "Favorites")
                            }
                            IconButton(
                                onClick = {},
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = "Favorites")
                            }
                        }
                    )
                }
            ) { paddingValues ->
                content(paddingValues)
            }
        }
    )
}

// Drawer content definition
@Composable
fun DrawerContent(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
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
