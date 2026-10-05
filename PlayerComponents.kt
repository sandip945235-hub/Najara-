package com.najara.app.ui

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// In-app ad (WebView + Close button)
@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun AdOverlayDialog(url: String, onClose: () -> Unit) {
    val webHolder = remember { arrayOfNulls<WebView>(1) }

    DisposableEffect(Unit) {
        onDispose {
            webHolder[0]?.stopLoading()
            webHolder[0]?.destroy()
            webHolder[0] = null
        }
    }

    Dialog(
        onDismissRequest = { },
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

// Thin seek bar: thin line + small orange dot
@Composable
internal fun PlayerSeekBar(
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
        modifier
            .height(28.dp)
            .pointerInput(Unit) {
                detectTapGestures { o ->
                    val f = (o.x / size.width).coerceIn(0f, 1f)
                    change(f)
                    done(f)
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { o ->
                        dragging = true
                        dragFraction = (o.x / size.width).coerceIn(0f, 1f)
                        change(dragFraction)
                    },
                    onDragEnd = {
                        dragging = false
                        done(dragFraction)
                    },
                    onDragCancel = {
                        dragging = false
                        done(dragFraction)
                    },
                    onHorizontalDrag = { c, _ ->
                        c.consume()
                        dragFraction = (c.position.x / size.width).coerceIn(0f, 1f)
                        change(dragFraction)
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val shown = (if (dragging) dragFraction else fraction).coerceIn(0f, 1f)
        val dotSize = 12.dp
        Box(
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color(0x55FFFFFF))
        )
        Box(
            Modifier
                .fillMaxWidth(shown)
                .height(2.dp)
                .background(orange)
        )
        Box(
            Modifier
                .offset(x = (maxWidth - dotSize) * shown)
                .size(dotSize)
                .clip(CircleShape)
                .background(orange)
        )
    }
}

// One bottom option (icon + label)
@Composable
internal fun PlayerOptionItem(icon: ImageVector, label: String, sub: String? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, label, tint = Color.White, modifier = Modifier.size(22.dp))
        Text(
            label,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
        if (sub != null) {
            Text(
                sub,
                color = Color(0xFFFFA500),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

// Time format: 01:23 or 1:02:03
internal fun playerFormatTime(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, s)
    else String.format("%02d:%02d", m, s)
}
