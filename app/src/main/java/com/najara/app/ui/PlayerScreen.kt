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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(title: String, videoUrl: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity

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

    LaunchedEffect(showControls) {
        if (showControls) {
            delay(3500)
            showControls = false
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
            modifier = Modifier.fillMaxSize().clickable { showControls = !showControls }
        )

        if (showControls && !isLocked) {
            Row(
                Modifier.fillMaxWidth().background(Color(0x99000000)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { exoPlayer.release(); onBack() }) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Text(title, color = Color.White, modifier = Modifier.weight(1f))
                IconButton(onClick = { isLocked = true }) {
                    Icon(Icons.Default.Lock, "Lock", tint = Color.White)
                }
            }
        }

        if (isLocked) {
            IconButton(
                onClick = { isLocked = false },
                modifier = Modifier.align(Alignment.CenterEnd).padding(16.dp)
            ) {
                Icon(Icons.Default.Lock, "Unlock", tint = Color.White)
            }
        }

        if (showControls && !isLocked) {
            Row(
                Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { exoPlayer.seekBack() }) {
                    Icon(Icons.Default.Replay10, "Back10", tint = Color.White,
                        modifier = Modifier.size(40.dp))
                }
                IconButton(onClick = {
                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                    isPlaying = exoPlayer.isPlaying
                }) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        "PlayPause", tint = Color.White, modifier = Modifier.size(56.dp)
                    )
                }
                IconButton(onClick = { exoPlayer.seekForward() }) {
                    Icon(Icons.Default.Forward10, "Fwd10", tint = Color.White,
                        modifier = Modifier.size(40.dp))
                }
            }

            Column(Modifier.align(Alignment.BottomCenter).padding(12.dp)) {
                LinearProgressIndicator(
                    progress = {
                        val dur = exoPlayer.duration.coerceAtLeast(1)
                        exoPlayer.currentPosition.toFloat() / dur
                    },
                    modifier = Modifier.fillMaxWidth().height(3.dp),
                    color = Color(0xFFE50914)
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Quality", color = Color.White)
                    Text("Subtitles", color = Color.White)
                    Text("Speed", color = Color.White)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }
}
