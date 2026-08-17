package com.opencode.mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.opencode.mobile.ui.screens.chat.ChatScreen
import com.opencode.mobile.ui.screens.sessions.SessionsScreen
import com.opencode.mobile.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    data object Sessions : Screen("sessions")
    data object Settings : Screen("settings")
    data object Chat : Screen("chat/{sessionId}") {
        fun createRoute(sessionId: String) = "chat/$sessionId"
    }
}

@Composable
fun OpenCodeNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Sessions.route) {
        composable(Screen.Sessions.route) {
            SessionsScreen(
                onSessionClick = { sessionId ->
                    navController.navigate(Screen.Chat.createRoute(sessionId))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: return@composable
            ChatScreen(
                sessionId = sessionId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
