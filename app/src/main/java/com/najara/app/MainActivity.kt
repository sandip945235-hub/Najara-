package com.najara.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.*
import com.najara.app.ui.*
import com.najara.app.ui.theme.NajaraTheme
import kotlinx.coroutines.delay

// ✅ अपना ऐड लिंक यहाँ डालें
private const val AD_URL = "https://asiafilm.org/4/600fe50678836cdbd92320c581b0107d"

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

            // पहले ऐड, Close के बाद वीडियो प्लेयर
            var adClosed by rememberSaveable { mutableStateOf(false) }

            if (!adClosed) {
                InAppAdDialog(
                    adUrl = AD_URL,
                    onClose = { adClosed = true }
                )
            } else {
                PlayerScreen(
                    title = title,
                    videoUrl = url,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun InAppAdDialog(adUrl: String, onClose: () -> Unit) {
    // Close बटन 5 सेकंड बाद चालू होगा, ताकि ऐड लोड होकर दिखे
    var secondsLeft by remember { mutableStateOf(5) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.javaScriptCanOpenWindowsAutomatically = true

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val url = request?.url?.toString() ?: return false
                                if (!url.contains("asiafilm.org")) {
                                    try {
                                        ctx.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        )
                                    } catch (e: Exception) {
                                    }
                                    return true
                                }
                                return false
                            }
                        }

                        loadUrl(adUrl)
                    }
                }
            )

            Button(
                onClick = onClose,
                enabled = secondsLeft == 0,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 40.dp)
            ) {
                Text(if (secondsLeft > 0) "Close in $secondsLeft" else "❌ Close Ad")
            }
        }
    }
}
