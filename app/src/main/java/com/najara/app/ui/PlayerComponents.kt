package com.najara.app.ui

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// 90000 ms -> "01:30", 3700000 ms -> "1:01:40"
fun playerFormatTime(ms: Long): String {
    val totalSec = (ms.coerceAtLeast(0L)) / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, s)
    else String.format("%02d:%02d", m, s)
}

@Composable
fun PlayerSeekBar(
    fraction: Float,
    onSeekChange: (Float) -> Unit,
    onSeekDone: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var lastValue by remember { mutableStateOf(fraction) }
    Slider(
        value = fraction.coerceIn(0f, 1f),
        onValueChange = {
            lastValue = it
            onSeekChange(it)
        },
        onValueChangeFinished = { onSeekDone(lastValue) },
        modifier = modifier,
        colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = Color.White,
            inactiveTrackColor = Color(0x66FFFFFF)
        )
    )
}

@Composable
fun PlayerOptionItem(icon: ImageVector, label: String, value: String? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(22.dp))
        Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall)
        if (value != null) {
            Text(value, color = Color(0xB3FFFFFF), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AdOverlayDialog(url: String, onClose: () -> Unit) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webViewClient = WebViewClient()
                        webChromeClient = WebChromeClient()
                        loadUrl(url)
                    }
                },
                onRelease = { it.destroy() },
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
                Icon(Icons.Default.Close, "Close ad", tint = Color.White)
            }
        }
    }
}
