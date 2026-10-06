package com.najara.app.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.messaging.FirebaseMessaging
import com.najara.app.NajaraMessagingService
import com.najara.app.data.Movie
import com.najara.app.data.MovieRepository
import com.najara.app.data.NotificationStore
import com.najara.app.data.ShareHelper
import kotlinx.coroutines.delay

// इंटरनेट चालू है या नहीं
private fun isNetworkAvailable(context: Context): Boolean {
    return try {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } catch (e: Exception) {
        true
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val repo = remember { MovieRepository() }
    var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var currentTab by remember { mutableStateOf("home") }

    // ===== इंटरनेट की स्थिति (हर 3 सेकंड में जाँच) =====
    var isOnline by remember { mutableStateOf(isNetworkAvailable(context)) }
    LaunchedEffect(Unit) {
        while (true) {
            isOnline = isNetworkAvailable(context)
            delay(3000)
        }
    }

    // ===== Share Dialog (48 hours) =====
    var showShareDialog by remember { mutableStateOf(false) }

    // ===== Notifications: अनुमति + ग्रुप + लाल निशान =====
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        NajaraMessagingService.createChannel(context)
        try {
            FirebaseMessaging.getInstance().subscribeToTopic("all")
        } catch (e: Exception) {
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        NotificationStore.syncFromTray(context)
    }

    // ऐप वापस खुलने पर स्टेटस बार की नई नोटिफिकेशन जोड़ो
    DisposableEffect(Unit) {
        val owner = context as? LifecycleOwner
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                NotificationStore.syncFromTray(context)
            }
        }
        owner?.lifecycle?.addObserver(observer)
        onDispose { owner?.lifecycle?.removeObserver(observer) }
    }

    val notifVersion = NotificationStore.version.value
    val unreadCount = remember(notifVersion) { NotificationStore.unreadCount(context) }

    // मूवी लिस्ट: इंटरनेट हो तभी लाओ, गड़बड़ी पर ऐप बंद न हो
    LaunchedEffect(isOnline) {
        if (isOnline) {
            if (movies.isEmpty()) {
                loading = true
                movies = try {
                    repo.fetchMovies()
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }
        loading = false
    }

    LaunchedEffect(Unit) {
        // 48 घंटे बाद share popup दिखाओ
        if (ShareHelper.shouldShowDialog(context)) {
            showShareDialog = true
            ShareHelper.markDialogShown(context)
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Column(Modifier.background(Color.Black)) {
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
                        Box {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White
                            )
                            if (unreadCount > 0) {
                                Box(
                                    Modifier
                                        .align(Alignment.TopEnd)
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE53935))
                                )
                            }
                        }
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
            if (loading && isOnline) {
                CircularProgressIndicator(
                    color = Color(0xFFE50914),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (movies.isEmpty()) {
                if (isOnline) {
                    Text(
                        "कोई मूवी नहीं मिली",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
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

            // ===== इंटरनेट नहीं है: संदेश + Downloads का रास्ता =====
            if (!isOnline) {
                Column(
                    Modifier
                        .align(if (movies.isEmpty()) Alignment.Center else Alignment.BottomCenter)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1A1A1A))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "इंटरनेट से नहीं जुड़े हैं",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "डाउनलोड किए हुए वीडियो देखने के लिए ऊपर Downloads आइकन दबाएँ",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { navController.navigate("downloads") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE50914)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Download, "Downloads", tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Downloads खोलें", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ===== Share Dialog Popup =====
        if (showShareDialog) {
            ShareDialog(onDismiss = { showShareDialog = false })
        }
    }
}
