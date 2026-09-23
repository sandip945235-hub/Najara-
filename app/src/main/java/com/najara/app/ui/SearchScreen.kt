package com.najara.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.najara.app.data.Movie
import com.najara.app.data.MovieRepository

@Composable
fun SearchScreen(navController: NavController) {
    val repo = remember { MovieRepository() }
    var allMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("All") }

    LaunchedEffect(Unit) { allMovies = repo.fetchMovies() }

    val categories = listOf("All") + allMovies.map { it.category }.distinct()
    val filtered = allMovies.filter {
        (category == "All" || it.category == category) &&
        it.title.contains(query, ignoreCase = true)
    }

    Column(Modifier.fillMaxSize().padding(8.dp)) {
        OutlinedTextField(
            value = query, onValueChange = { query = it },
            label = { Text("Search movies...") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFE50914), unfocusedBorderColor = Color.Gray
            )
        )

        Spacer(Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(categories.size) { i ->
                val c = categories[i]
                FilterChip(
                    selected = c == category,
                    onClick = { category = c },
                    label = { Text(c, color = Color.White) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF1A1A1A),
                        selectedContainerColor = Color(0xFFE50914)
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filtered) { movie ->
                AsyncImage(
                    model = movie.poster, contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth().aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val encoded = android.net.Uri.encode(movie.embedLink)
                            val t = android.net.Uri.encode(movie.title)
                            navController.navigate("player/$t/$encoded")
                        }
                )
            }
        }
    }
}
