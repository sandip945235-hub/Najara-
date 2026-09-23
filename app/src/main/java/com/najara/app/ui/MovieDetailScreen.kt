package com.najara.app.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.najara.app.data.Movie
import com.najara.app.data.MovieRepository

@Composable
fun MovieDetailScreen(navController: NavController, movieTitle: String) {
    val context = LocalContext.current
    val repo = remember { MovieRepository() }
    var movie by remember { mutableStateOf<Movie?>(null) }
    var allMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val list = repo.fetchMovies()
        allMovies = list
        movie = list.firstOrNull { it.title == movieTitle }
        loading = false
    }

    val trailerUrl = movie?.trailer?.takeIf { it.isNotBlank() }
    val miniPlayer = remember {
        if (trailerUrl != null) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(trailerUrl))
                prepare()
                playWhenReady = true
                volume = 0f
                repeatMode = ExoPlayer.REPEAT_MODE_ALL
            }
        } else null
    }

    DisposableEffect(Unit) {
        onDispose { miniPlayer?.release() }
    }

    if (loading) {
        Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFE50914))
        }
        return
    }

    if (movie == null) {
        Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Text("मूवी नहीं मिली", color = Color.White)
        }
        return
    }

    val m = movie!!

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black)
        ) {
            if (miniPlayer != null) {
                AndroidView(
                    factory = {
                        PlayerView(it).apply {
                            player = miniPlayer
                            useController = false
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AsyncImage(
                    model = m.poster,
                    contentDescription = m.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            IconButton(
                onClick = {
                    isMuted = !isMuted
                    miniPlayer?.volume = if (isMuted) 0f else 1f
                },
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
            ) {
                Icon(
                    if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    "Volume",
                    tint = Color.White
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            m.title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(12.dp))

        Row(Modifier.padding(horizontal = 16.dp)) {
            Column(Modifier.weight(1f)) {
                DetailRow("Print", m.print)
                DetailRow("Industry", m.industry)
                DetailRow("Category", m.category)
                DetailRow("Language", m.language)
                DetailRow("Quality", m.quality)
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = m.poster,
                    contentDescription = m.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(100.dp)
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(8.dp))
                )
                if (m.rating.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "⭐ ${m.rating}",
                        color = Color(0xFFFFC107),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Button(
                onClick = {
                    val t = android.net.Uri.encode(m.title)
                    val u = android.net.Uri.encode(m.embedLink)
                    navController.navigate("player/$t/$u")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp)
            ) {
                Icon(Icons.Default.PlayArrow, "Play", tint = Color.Black)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Watch Now",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(60.dp)
                .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Ads", color = Color.Gray)
        }

        Spacer(Modifier.height(16.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionIcon(Icons.Default.FavoriteBorder, "Like") { }
            ActionIcon(Icons.Default.Download, "Download") { }
            ActionIcon(Icons.Default.Add, "My List") { }
            ActionIcon(Icons.Default.Share, "Share") {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Najara App देखें! 🎬 मूवीज़ देखने के लिए डाउनलोड करें।"
                    )
                }
                context.startActivity(Intent.createChooser(intent, "Share via"))
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Recommended",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 2000.dp)
        ) {
            items(allMovies.filter { it.category == m.category && it.title != m.title }) { rec ->
                AsyncImage(
                    model = rec.poster,
                    contentDescription = rec.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val t = android.net.Uri.encode(rec.title)
                            navController.navigate("detail/$t")
                        }
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(Modifier.padding(vertical = 3.dp)) {
        Text(
            "$label:",
            color = Color(0xFFFFA500),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(90.dp)
        )
        Text(
            value,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ActionIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(icon, label, tint = Color.White, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}
