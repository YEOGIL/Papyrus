package com.intel.papyrusbaby.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.intel.papyrusbaby.firebase.AuthScreenEmailPassword
import com.intel.papyrusbaby.screen.ArchivedListContentsScreen
import com.intel.papyrusbaby.screen.ArchivedListScreen
import com.intel.papyrusbaby.screen.HomeScreen
import com.intel.papyrusbaby.screen.ImageGenerationScreen
import com.intel.papyrusbaby.screen.WriteLetterScreen
import com.intel.papyrusbaby.screen.WrittenLetterScreen

// 네비게이션 경로를 중앙 관리하는 sealed class
sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object Home : Screen("home")
    data object Write : Screen("write?writer={writer}") {
        fun createRoute(writer: String) = "write?writer=$writer"
    }
    data object WrittenLetter : Screen("writtenLetter?writer={writer}&documentType={documentType}&prompt={prompt}&theme={theme}") {
        fun createRoute(writer: String, documentType: String, prompt: String, theme: String) =
            "writtenLetter?writer=$writer&documentType=$documentType&prompt=$prompt&theme=$theme"
    }
    data object Archive : Screen("archive")
    data object ArchiveDetail : Screen("archiveDetail/{docId}") {
        fun createRoute(docId: String) = "archiveDetail/$docId"
    }
    data object ImageGeneration : Screen("imageGeneration?letterText={letterText}") {
        fun createRoute(letterText: String) = "imageGeneration?letterText=${letterText}"
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // 인증 화면
        composable(Screen.Auth.route) {
            AuthScreenEmailPassword(navController) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
            }
        }
        // 홈 화면
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        // 작성 화면 (파라미터: writer)
        composable(
            route = Screen.Write.route,
            arguments = listOf(navArgument("writer") { defaultValue = "" })
        ) { backStackEntry ->
            val writer = backStackEntry.arguments?.getString("writer") ?: ""
            WriteLetterScreen(navController, writer)
        }
        // 작성 완료된 글 화면 (파라미터: writer, documentType, prompt, theme)
        composable(
            route = Screen.WrittenLetter.route,
            arguments = listOf(
                navArgument("writer") { defaultValue = "" },
                navArgument("documentType") { defaultValue = "" },
                navArgument("prompt") { defaultValue = "" },
                navArgument("theme") { defaultValue = "" }
            )
        ) { backStackEntry ->
            val writer = backStackEntry.arguments?.getString("writer") ?: ""
            val documentType = backStackEntry.arguments?.getString("documentType") ?: ""
            val prompt = backStackEntry.arguments?.getString("prompt") ?: ""
            val theme = backStackEntry.arguments?.getString("theme") ?: ""
            WrittenLetterScreen(writer, documentType, prompt, theme, navController)
        }
        // 보관함 목록
        composable(Screen.Archive.route) {
            ArchivedListScreen(navController)
        }
        // 보관함 상세 화면 (파라미터: docId)
        composable(
            route = Screen.ArchiveDetail.route,
            arguments = listOf(navArgument("docId") { defaultValue = "" })
        ) { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId") ?: ""
            ArchivedListContentsScreen(docId, navController)
        }
        // 이미지 생성 화면 (파라미터: letterText)
        composable(
            route = Screen.ImageGeneration.route,
            arguments = listOf(navArgument("letterText") { defaultValue = "" })
        ) { backStackEntry ->
            val letterText = backStackEntry.arguments?.getString("letterText") ?: ""
            ImageGenerationScreen(letterText)
        }
    }
}
