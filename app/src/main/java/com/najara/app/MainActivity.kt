package com.najara.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.najara.app.ui.*
import com.najara.app.ui.theme.NajaraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NajaraTheme { NajaraApp() }
        }
    }
}

@Composable
fun NajaraApp() {
    val navController = rememberNavController()
    val items = listOf("home" to "Movie", "search" to "Search", "settings" to "Settings")
    val icons = listOf(Icons.Default.Home, Icons.Default.Search, Icons.Default.Settings)

    Scaffold(
        topBar = {
            Column(Modifier.background(Color.Black)) {
                Row(Modifier.fillMaxWidth().padding(12.dp)) {
                    Text(
                        "🎬 NAJARA",
                        color = Color(0xFFE50914),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                TabRow(
                    selectedTabIndex = 0,
                    containerColor = Color.Black,
                    contentColor = Color(0xFFE50914)
                ) {
                    items.forEachIndexed { i, item ->
                        Tab(
                            selected = i == 0,
                            onClick = {
                                navController.navigate(item.first) {
                                    popUpTo("home"); launchSingleTop = true
                                }
                            },
                            text = { Text(item.second, color = Color.White) }
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF0A0A0A)) {
                val navBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStack?.destination?.route
                items.forEachIndexed { i, item ->
                    NavigationBarItem(
                        selected = currentRoute == item.first,
                        onClick = {
                            navController.navigate(item.first) {
                                popUpTo("home"); launchSingleTop = true
                            }
                        },
                        icon = {
                            Icon(
                                icons[i],
                                contentDescription = item.second,
                                tint = if (currentRoute == item.first) Color(0xFFE50914) else Color.White
                            )
                        },
                        label = { Text(item.second, color = Color.White) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController,
            startDestination = "home",
            modifier = Modifier.padding(padding).background(Color.Black)
        ) {
            composable("home") { HomeScreen(navController) }
            composable("search") { SearchScreen(navController) }
            composable("settings") { SettingsScreen() }
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
}
