package com.najara.app.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.provider.Settings
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

// ===== AD सेटिंग =====
// टेस्ट करना हो तो 30 * 60 * 1000L की जगह 60 * 1000L (1 मिनट) कर दें
private const val AD_INTERVAL_MS = 30 * 60 * 1000L
private const val AD_URL = "https://asiafilm.org/4/600fe50678836cdbd92320c581b0107d"

@Composable
fun PlayerScreen(title: String, videoUrl: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity

    // ===== Auto Landscape + Full Screen (समय, बैटरी, नेटवर्क, नीचे के बटन सब छुपे) =====
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        val window = activity?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, it.decorView) }
        controller?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            controller?.show(WindowInsetsCompat.Type.systemBars())
            // बाहर निकलते समय फ़ोन की असली ब्राइटनेस वापस
            window?.let {
                val lp = it.attributes
                lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                it.attributes = lp
            }
        }
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
            prepare()
            playWhenReady = true
        }
    }

    var showControls by remember { mutableStateOf(true) }
    var isLocked by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentPos by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var isDragging by remember { mutableStateOf(false) }
    var zoomLevel by remember { mutableStateOf(0) }  // 0=Normal, 1=Medium, 2=Full
    var zoomToast by remember { mutableStateOf("") }

    // ===== AD स्टेट =====
    var showAd by remember { mutableStateOf(false) }
    var playedMs by remember { mutableStateOf(0L) }  // सिर्फ़ असली प्ले का समय गिनता है

    // ===== हर 30 मिनट के प्लेबैक पर ऐड (pause होने पर / ऐप बैकग्राउंड में होने पर टाइमर नहीं बढ़ता) =====
    LaunchedEffect(Unit) {
        val owner = context as? LifecycleOwner
        while (true) {
            delay(1000)
            val screenVisible =
                owner?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) ?: true
            if (!showAd && screenVisible && exoPlayer.isPlaying) {
                playedMs += 1000
                if (playedMs >= AD_INTERVAL_MS) {
                    exoPlayer.pause()      // 1. वीडियो रोकें
                    playedMs = 0L
                    showAd = true          // 2. ऐड खोलें
                }
            }
        }
    }

    // ===== Brightness (बाईं तरफ़ ऊपर/नीचे स्वाइप) =====
    var brightness by remember {
        mutableStateOf(
            run {
                val current = activity?.window?.attributes?.screenBrightness ?: -1f
                if (current >= 0f) current
                else runCatching {
                    Settings.System.getInt(
                        context.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS
                    ) / 255f
                }.getOrDefault(0.5f)
            }
        )
    }
    var showBrightness by remember { mutableStateOf(false) }

    fun applyBrightness(value: Float) {
        brightness = value.coerceIn(0.02f, 1f)
        activity?.window?.let {
            val lp = it.attributes
            lp.screenBrightness = brightness
            it.attributes = lp
        }
        showBrightness = true
    }

    // ब्राइटनेस पट्टी 1 सेकंड बाद छुपाना
    LaunchedEffect(brightness) {
        delay(1000)
        showBrightness = false
    }

    val videoScale = when (zoomLevel) {
        0 -> 1.0f
        1 -> 1.15f
        else -> 1.30f
    }

    // ===== ज़ूम का नाम 1 सेकंड दिखाकर छुपाना =====
    LaunchedEffect(zoomToast) {
        if (zoomToast.isNotEmpty()) {
            delay(1200)
            zoomToast = ""
        }
    }

    // ===== Auto-hide controls (4 sec) =====
    LaunchedEffect(showControls, isLocked, isDragging, zoomLevel) {
        if (showControls && !isLocked && !isDragging) {
            delay(4000)
            showControls = false
        }
    }

    // ===== Position update loop =====
    LaunchedEffect(Unit) {
        while (true) {
            if (!isDragging) currentPos = exoPlayer.currentPosition
            duration = exoPlayer.duration.coerceAtLeast(0)
            isPlaying = exoPlayer.isPlaying
            delay(500)
        }
    }

    BackHandler {
        exoPlayer.release()
        onBack()
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {

        // ===== VIDEO (with Zoom) =====
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .scale(videoScale)
                .clickable(enabled = !isLocked) {
                    showControls = !showControls
                }
        )

        // ===== बाईं तरफ़ ब्राइटनेस स्वाइप एरिया (लॉक में बंद) =====
        if (!isLocked) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .fillMaxWidth(0.4f)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { showControls = !showControls })
                    }
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { change, dragAmount ->
                            change.consume()
                            // ऊपर ले जाओ = रोशनी बढ़े, नीचे = कम
                            applyBrightness(brightness - (dragAmount / size.height) * 1.5f)
                        }
                    }
            )
        }

        // ===== TOP BAR (कोई काली पट्टी नहीं - सिर्फ gradient) =====
        if (showControls && !isLocked) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xAA000000), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { exoPlayer.release(); onBack() }) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Text(
                    title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = { /* Cast */ }) {
                    Icon(Icons.Default.Cast, "Cast", tint = Color.White)
                }
                // ===== ZOOM BUTTON (ऊपर, Cast के बगल में) =====
                IconButton(onClick = {
                    zoomLevel = (zoomLevel + 1) % 3
                    zoomToast = when (zoomLevel) {
                        0 -> "Normal"
                        1 -> "Medium"
                        else -> "Full"
                    }
                }) {
                    Icon(Icons.Default.AspectRatio, "Zoom", tint = Color.White)
                }
                IconButton(onClick = { /* Volume */ }) {
                    Icon(Icons.Default.VolumeUp, "Volume", tint = Color.White)
                }
                IconButton(onClick = { /* Share */ }) {
                    Icon(Icons.Default.Share, "Share", tint = Color.White)
                }
                IconButton(onClick = { isLocked = true }) {
                    Icon(Icons.Default.Lock, "Lock", tint = Color.White)
                }
            }
        }

        // ===== UNLOCK BUTTON (सिर्फ Locked में) =====
        if (isLocked) {
            IconButton(
                onClick = { isLocked = false },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(24.dp)
                    .clip(CircleShape)
                    .background(Color(0x88000000))
            ) {
                Icon(
                    Icons.Default.Lock,
                    "Unlock",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // ===== CENTER CONTROLS =====
        if (showControls && !isLocked) {
            Row(
                Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // -10s
                IconButton(onClick = {
                    val newPos = (exoPlayer.currentPosition - 10000).coerceAtLeast(0)
                    exoPlayer.seekTo(newPos)
                }) {
                    Icon(
                        Icons.Default.Replay10, "Back10",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
                // Play/Pause
                IconButton(onClick = {
                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                    isPlaying = exoPlayer.isPlaying
                }) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        "PlayPause",
                        tint = Color.White,
                        modifier = Modifier.size(72.dp)
                    )
                }
                // +10s
                IconButton(onClick = {
                    val newPos = (exoPlayer.currentPosition + 10000).coerceAtMost(duration)
                    exoPlayer.seekTo(newPos)
                }) {
                    Icon(
                        Icons.Default.Forward10, "Fwd10",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        // ===== ब्राइटनेस पट्टी (सूरज + खड़ी लाइन) =====
        if (showBrightness && !isLocked) {
            Column(
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.WbSunny,
                    "Brightness",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.height(10.dp))
                Box(
                    Modifier
                        .width(4.dp)
                        .height(150.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0x66FFFFFF)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(brightness.coerceIn(0f, 1f))
                            .background(Color.White)
                    )
                }
            }
        }

        // ===== ZOOM का नाम (Normal / Medium / Full) =====
        if (zoomToast.isNotEmpty()) {
            Text(
                zoomToast,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x99000000))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // ===== BOTTOM BAR (कोई काली पट्टी नहीं - सिर्फ gradient) =====
        if (showControls && !isLocked) {
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xCC000000))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // ===== पतली Progress Bar =====
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        formatTime(currentPos),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(Modifier.width(10.dp))
                    ThinSeekBar(
                        fraction = if (duration > 0) currentPos.toFloat() / duration else 0f,
                        onSeekChange = { f ->
                            isDragging = true
                            currentPos = (f * duration).toLong()
                        },
                        onSeekDone = { f ->
                            exoPlayer.seekTo((f * duration).toLong())
                            isDragging = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        formatTime(duration),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(Modifier.height(4.dp))

                // ===== Bottom Options =====
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PlayerOption(Icons.Default.Movie, "Recommended")
                    PlayerOption(Icons.Default.HighQuality, "Quality", "Auto")
                    PlayerOption(Icons.Default.MusicNote, "Audio")
                    PlayerOption(Icons.Default.SkipNext, "Next")
                    PlayerOption(Icons.Default.Speed, "Speed", "Normal")
                }
            }
        }
    }

    // ===== IN-APP AD (ऐड बंद होते ही वीडियो वहीं से चालू) =====
    if (showAd) {
        InAppAdDialog(
            url = AD_URL,
            onClose = {
                showAd = false
                exoPlayer.play()   // 3. वीडियो वापस चालू
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }
}

// ===== ऐप के अंदर खुलने वाला ऐड (WebView + Close बटन) =====
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun InAppAdDialog(url: String, onClose: () -> Unit) {
    val webHolder = remember { arrayOfNulls<WebView>(1) }

    DisposableEffect(Unit) {
        onDispose {
            webHolder[0]?.stopLoading()
            webHolder[0]?.destroy()
            webHolder[0] = null
        }
    }

    Dialog(
        onDismissRequest = { /* बैक दबाने से बंद नहीं होगा, Close बटन से ही बंद */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webViewClient = object : WebViewClient() {
                            // ऐड के अंदर ही रहे; market:// intent:// जैसे बाहरी लिंक रोक दें
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val scheme = request?.url?.scheme ?: return false
                                return scheme != "http" && scheme != "https"
                            }
                        }
                        loadUrl(url)
                        webHolder[0] = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(CircleShape)
                    .background(Color(0xAA000000))
            ) {
                Icon(Icons.Default.Close, "Close", tint = Color.White)
            }
        }
    }
}

// ===== पतली सीक-बार: पतली लाइन + छोटा नारंगी बिंदु =====
@Composable
private fun ThinSeekBar(
    fraction: Float,
    onSeekChange: (Float) -> Unit,
    onSeekDone: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val change by rememberUpdatedState(onSeekChange)
    val done by rememberUpdatedState(onSeekDone)
    var dragFraction by remember { mutableStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    val orange = Color(0xFFFFA500)

    BoxWithConstraints(
        modifie
