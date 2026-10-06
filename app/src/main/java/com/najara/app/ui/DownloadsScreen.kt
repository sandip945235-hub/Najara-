package com.najara.app.ui

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.najara.app.data.DownloadItem
import com.najara.app.data.DownloadProgress
import com.najara.app.data.DownloadStore
import kotlinx.coroutines.delay
import java.util.Locale

private fun loadProgress(
    context: Context,
    list: List<DownloadItem>
): Map<Long, DownloadProgress?> =
    list.associate { it.id to DownloadStore.query(context, it.id) }

private fun formatBytes(b: Long): String {
    if (b <= 0L) return "0 MB"
    val mb = b / (1024.0 * 1024.0)
    return if (mb >= 1024) String.format(Locale.US, "%.2f GB", mb / 1024)
    else String.format(Locale.US, "%.1f MB", mb)
}

@Composable
fun DownloadsScreen(navController: NavController) {
    val context = LocalContext.current
    var downloadItems by remember { mutableStateOf(DownloadStore.getAll(context)) }
    var progress by remember { mutableStateOf(loadProgress(context, downloadItems)) }

    // हर सेकंड प्रोग्रेस अपडेट
    LaunchedEffect(Unit) {
        while (true) {
            val list = DownloadStore.getAll(context)
            downloadItems = list
            progress = loadProgress(context, list)
            delay(1000)
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Text(
                    "Downloads",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
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
            if (downloadItems.isEmpty()) {
                Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        "Empty",
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "कोई डाउनलोड नहीं",
                        color = Color.Gray,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "जो मूवी डाउनलोड करेंगे, वो यहाँ दिखेंगी",
                        color = Color.DarkGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(downloadItems, key = { it.id }) { item ->
                        val p = progress[item.id]
                        val file = DownloadStore.fileFor(context, item.fileName)
                        val done = p?.status == DownloadManager.STATUS_SUCCESSFUL ||
                                (p == null && file.exists())
                        val failed = p?.status == DownloadManager.STATUS_FAILED ||
                                (p == null && !file.exists())
                        val paused = p?.status == DownloadManager.STATUS_PAUSED
                        val frac: Float = if (p != null && p.total > 0)
                            (p.downloaded.toFloat() / p.total.toFloat()).coerceIn(0f, 1f) else 0f

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1A1A1A))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    item.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2
                                )
                                Spacer(Modifier.height(6.dp))
                                when {
                                    done -> Text(
                                        "डाउनलोड पूरा • ${formatBytes(file.length())}",
                                        color = Color(0xFF4CAF50),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    failed -> Text(
                                        "डाउनलोड फेल हुआ",
                                        color = Color(0xFFE53935),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    else -> {
                                        LinearProgressIndicator(
                                            progress = frac,
                                            modifier = Modifier.fillMaxWidth(),
                                            color = Color(0xFFE50914),
                                            trackColor = Color(0xFF333333)
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            if (paused) "रुका है — नेटवर्क का इंतज़ार"
                                            else "${(frac * 100).toInt()}% • ${formatBytes(p?.downloaded ?: 0L)} / ${formatBytes(p?.total ?: 0L)}",
                                            color = Color.Gray,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                            if (done) {
                                IconButton(onClick = {
                                    val t = Uri.encode(item.title)
                                    val u = Uri.encode(Uri.fromFile(file).toString())
                                    navController.navigate("player/$t/$u")
                                }) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        "Play",
                                        tint = Color(0xFFFFA500),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            IconButton(onClick = {
                                DownloadStore.remove(context, item)
                                downloadItems = DownloadStore.getAll(context)
                            }) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFE50914))
                            }
                        }
                    }
                }
            }
        }
    }
}
