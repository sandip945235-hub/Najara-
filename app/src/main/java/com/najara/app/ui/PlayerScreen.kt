package com.najara.app.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(title: String, videoUrl: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity

    // ===== Auto Landscape + Sensor (phone घुमाओ → video घूमे) =====
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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
    var zoomLevel by remember { mutableStateOf(0) }  // 0=Normal, 1=Medium, 2=Full

    val videoScale = when (zoomLevel) {
        0 -> 1.0f
        1 -> 1.15f
        else -> 1.30f
    }

    // ===== Auto-hide controls (4 sec) =====
    LaunchedEffect(showControls, isLocked) {
        if (showControls && !isLocked) {
            delay(4000)
            showControls = false
        }
    }

    // ===== Position update loop =====
    LaunchedEffect(Unit) {
        while (true) {
            currentPos = exoPlayer.currentPosition
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

            // ===== ZOOM BUTTON (Center के नीचे) =====
            Column(
                Modifier
                    .align(Alignment.Center)
                    .offset(y = 90.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = { zoomLevel = (zoomLevel + 1) % 3 },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x88000000))
                ) {
                    Icon(
                        Icons.Default.ZoomIn,
                        "Zoom",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    when (zoomLevel) {
                        0 -> "Normal"
                        1 -> "Medium"
                        else -> "Full"
                    },
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
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
                // ===== Thin Progress Bar =====
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        formatTime(currentPos),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(Modifier.width(10.dp))
                    Slider(
                        value = if (duration > 0) currentPos.toFloat() else 0f,
                        onValueChange = { currentPos = it.toLong() },
                        onValueChangeFinished = { exoPlayer.seekTo(currentPos) },
                        valueRange = 0f..(if (duration > 0) duration.toFloat() else 1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFA500),
                            activeTrackColor = Color(0xFFFFA500),
                            inactiveTrackColor = Color(0x66FFFFFF)
                        ),
                        modifier = Modifier.weight(1f).height(20.dp)
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

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }
}

@Composable
private fun PlayerOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    subLabel: String = ""
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, label, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Column {
            Text(
                label,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (subLabel.isNotBlank()) {
                Text(
                    subLabel,
                    color = Color(0xFFBDBDBD),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0) return "00:00"
    val totalSec = ms / 1000
    val hours = totalSec / 3600
    val minutes = (totalSec % 3600) / 60
    val seconds = totalSec % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
