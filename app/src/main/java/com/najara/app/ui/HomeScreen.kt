package com.najara.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.najara.app.data.Movie
import com.najara.app.data.MovieRepository

@Composable
fun HomeScreen(navController: NavController) {
    val repo = remember { MovieRepository() }
    var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var currentTab by remember { mutableStateOf("home") }

    LaunchedEffect(Unit) {
        movies = repo.fetchMovies()
        loading = false
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Column(Modifier.background(Color.Black)) {
                // ===== Top Bar: Logo + 3 icons =====
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🎬 NAJARA",
                        color = Color(0xFFE50914),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = {
                        navController.navigate("notifications")
                    }) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = {
                        // Showpiece — कुछ नहीं होगा
                    }) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = "Message",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = {
                        navController.navigate("downloads")
                    }) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Downloads",
                            tint = Color.White
                        )
                    }
                }
                // ===== Movie Tab =====
                TabRow(
                    selectedTabIndex = 0,
                    containerColor = Color.Black,
                    contentColor = Color(0xFFE50914)
                ) {
                    Tab(
                        selected = true,
                        onClick = { },
                        text = {
                            Text("Movie", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF0A0A0A)) {
                NavigationBarItem(
                    selected = currentTab == "home",
                    onClick = { currentTab = "home" },
                    icon = { Icon(Icons.Default.Home, "Home",
                        tint = if (currentTab == "home") Color(0xFFE50914) else Color.White) },
                    label = { Text("Home", color = Color.White) }
                )
                NavigationBarItem(
                    selected = currentTab == "search",
                    onClick = {
                        currentTab = "search"
                        navController.navigate("search")
                    },
                    icon = { Icon(Icons.Default.Search, "Search",
                        tint = if (currentTab == "search") Color(0xFFE50914) else Color.White) },
                    label = { Text("Search", color = Color.White) }
                )
                NavigationBarItem(
                    selected = currentTab == "settings",
                    onClick = {
                        currentTab = "settings"
                        navController.navigate("settings")
                    },
                    icon = { Icon(Icons.Default.Settings, "Settings",
                        tint = if (currentTab == "settings") Color(0xFFE50914) else Color.White) },
                    label = { Text("Settings", color = Color.White) }
                )
            }
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    color = Color(0xFFE50914),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (movies.isEmpty()) {
                Text(
                    "कोई मूवी नहीं मिली",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(movies) { movie ->
                        Column(
                            Modifier.clickable {
                                val t = android.net.Uri.encode(movie.title)
                                navController.navigate("detail/$t")
                            }
                        ) {
                            AsyncImage(
                                model = movie.poster,
                                contentDescription = movie.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(2f / 3f)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}
