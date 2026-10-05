package com.najara.app.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.provider.Settings
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

// AD setting: for testing use 60 * 1000L (1 minute)
private const val AD_INTERVAL_MS = 30 * 60 * 1000L
private const val AD_URL = "https://asiafilm.org/4/600fe50678836cdbd92320c581b0107d"

@Composable
fun PlayerScreen(title: String, videoUrl: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Landscape + full screen
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
    var zoomLevel by remember { mutableStateOf(0) }
    var zoomToast by remember { mutableStateOf("") }

    // Ad state
    var showAd by remember { mutableStateOf(false) }
    var playedMs by remember { mutableStateOf(0L) }

    // Ad every 30 minutes of real playback
    LaunchedEffect(Unit) {
        val owner = context as? LifecycleOwner
        while (true) {
            delay(1000)
            val screenVisible =
                owner?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) ?: true
            if (!showAd && screenVisible && exoPlayer.isPlaying) {
                playedMs += 1000
                if (playedMs >= AD_INTERVAL_MS) {
                    exoPlayer.pause()
                    playedMs = 0L
                    showAd = true
                }
            }
        }
    }

    // Brightness
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

    LaunchedEffect(brightness) {
        delay(1000)
        showBrightness = false
    }

    val videoScale = when (zoomLevel) {
        0 -> 1.0f
        1 -> 1.15f
        else -> 1.30f
    }

    LaunchedEffect(zoomToast) {
        if (zoomToast.isNotEmpty()) {
            delay(1200)
            zoomToast = ""
        }
    }

    LaunchedEffect(showControls, isLocked, isDragging, zoomLevel) {
        if (showControls && !isLocked && !isDragging) {
            delay(4000)
            showControls = false
        }
    }

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

        // Video with zoom
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

        // Left side brightness swipe area
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
                            applyBrightness(brightness - (dragAmount / size.height) * 1.5f)
                        }
                    }
            )
        }

        // Top bar
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
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Cast, "Cast", tint = Color.White)
                }
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
                IconButton(onClick = { }) {
                    Icon(Icons.Default.VolumeUp, "Volume", tint = Color.White)
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Share, "Share", tint = Color.White)
                }
                IconButton(onClick = { isLocked = true }) {
                    Icon(Icons.Default.Lock, "Lock", tint = Color.White)
                }
            }
        }

        // Unlock button
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

        // Center controls
        if (showControls && !isLocked) {
            Row(
                Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0))
                }) {
                    Icon(
                        Icons.Default.Replay10, "Back10",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
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
                IconButton(onClick = {
                    exoPlayer.seekTo((exoPlayer.currentPosition + 10000).coerceAtMost(duration))
                }) {
                    Icon(
                        Icons.Default.Forward10, "Fwd10",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        // Brightness bar
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

        // Zoom name
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

        // Bottom bar
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        playerFormatTime(currentPos),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(Modifier.width(10.dp))
                    PlayerSeekBar(
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
                        playerFormatTime(duration),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PlayerOptionItem(Icons.Default.Movie, "Recommended")
                    PlayerOptionItem(Icons.Default.HighQuality, "Quality", "Auto")
                    PlayerOptionItem(Icons.Default.MusicNote, "Audio")
                    PlayerOptionItem(Icons.Default.SkipNext, "Next")
                    PlayerOptionItem(Icons.Default.Speed, "Speed", "Normal")
                }
            }
        }
    }

    // In-app ad: video resumes when ad is closed
    if (showAd) {
        AdOverlayDialog(
            url = AD_URL,
            onClose = {
                showAd = false
                exoPlayer.play()
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }
}
