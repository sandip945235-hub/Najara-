package com.najara.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.najara.app.ui.*
import com.najara.app.ui.theme.NajaraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NajaraTheme {
                NajaraApp()
            }
        }
    }
}

@Composable
fun NajaraApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(navController)
        }
        composable("search") {
            SearchScreen(navController)
        }
        composable("settings") {
            SettingsScreen(navController)
        }
        composable("privacy") {
            PrivacyPolicyScreen(navController)
        }
        composable("downloads") {
            DownloadsScreen(navController)
        }
        composable("notifications") {
            NotificationsScreen(navController)
        }
        composable("detail/{title}") { backStack ->
            val title = backStack.arguments?.getString("title") ?: ""
            MovieDetailScreen(navController, title)
        }
        composable("player/{title}/{url}") { backStack ->
            val title = backStack.arguments?.getString("title") ?: ""
            val url = backStack.arguments?.getString("url") ?: ""
            PlayerScreen(
                title = title,
                videoUrl = url,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
