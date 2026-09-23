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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

    // ===== Landscape fullscreen =====
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
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

    // ===== Auto hide controls (4 sec) =====
    LaunchedEffect(showControls) {
        if (showControls && !isLocked) {
            delay(4000)
            showControls = false
        }
    }

    // ===== Update position every 500ms =====
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
            modifier = Modifier.fillMaxSize().clickable {
                if (!isLocked) showControls = !showControls
            }
        )

        // ===== Top Bar =====
        if (showControls && !isLocked) {
            Row(
                Modifier.fillMaxWidth().background(Color(0x99000000)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { exoPlayer.release(); onBack() }) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Text(
                    title,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = { isLocked = true }) {
                    Icon(Icons.Default.Lock, "Lock", tint = Color.White)
                }
            }
        }

        // ===== Unlock button (when locked) =====
        if (isLocked) {
            IconButton(
                onClick = { isLocked = false },
                modifier = Modifier.align(Alignment.CenterEnd).padding(16.dp)
            ) {
                Icon(Icons.Default.Lock, "Unlock", tint = Color.White,
                    modifier = Modifier.size(32.dp))
            }
        }

        // ===== Center Controls =====
        if (showControls && !isLocked) {
            Row(
                Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 10s back
                IconButton(onClick = {
                    val newPos = (exoPlayer.currentPosition - 10000).coerceAtLeast(0)
                    exoPlayer.seekTo(newPos)
                }) {
                    Icon(Icons.Default.Replay10, "Back10", tint = Color.White,
                        modifier = Modifier.size(44.dp))
                }
                // Play/Pause
                IconButton(onClick = {
                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                    isPlaying = exoPlayer.isPlaying
                }) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        "PlayPause", tint = Color.White, modifier = Modifier.size(64.dp)
                    )
                }
                // 10s forward
                IconButton(onClick = {
                    val newPos = (exoPlayer.currentPosition + 10000).coerceAtMost(duration)
                    exoPlayer.seekTo(newPos)
                }) {
                    Icon(Icons.Default.Forward10, "Fwd10", tint = Color.White,
                        modifier = Modifier.size(44.dp))
                }
            }
        }

        // ===== Bottom Seekbar + Time + Options =====
        if (showControls && !isLocked) {
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0x99000000))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Time + Seekbar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        formatTime(currentPos),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.width(8.dp))
                    Slider(
                        value = if (duration > 0) currentPos.toFloat() else 0f,
                        onValueChange = { newValue ->
                            currentPos = newValue.toLong()
                        },
                        onValueChangeFinished = {
                            exoPlayer.seekTo(currentPos)
                        },
                        valueRange = 0f..(if (duration > 0) duration.toFloat() else 1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFA500),
                            activeTrackColor = Color(0xFFFFA500),
                            inactiveTrackColor = Color(0x66FFFFFF)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        formatTime(duration),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Spacer(Modifier.height(6.dp))

                // Options bar (display only)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PlayerOption(Icons.Default.Movie, "Recommended")
                    PlayerOption(Icons.Default.HighQuality, "Quality")
                    PlayerOption(Icons.Default.MusicNote, "Audio")
                    PlayerOption(Icons.Default.SkipNext, "Next")
                    PlayerOption(Icons.Default.Speed, "Speed")
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
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, label, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall)
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
