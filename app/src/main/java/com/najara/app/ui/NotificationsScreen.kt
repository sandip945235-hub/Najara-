package com.najara.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
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
import com.najara.app.data.NotificationStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(navController: NavController) {
    val context = LocalContext.current
    var newIds by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(Unit) {
        NotificationStore.syncFromTray(context)
        newIds = NotificationStore.getAll(context).filter { !it.read }.map { it.id }.toSet()
        NotificationStore.markAllRead(context)
    }

    val version = NotificationStore.version.value
    val list = remember(version) { NotificationStore.getAll(context) }
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

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
                    "Notifications",
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
            if (list.isEmpty()) {
                Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        "Empty",
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "कोई नोटिफिकेशन नहीं",
                        color = Color.Gray,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "नई अपडेट्स यहाँ दिखेंगी",
                        color = Color.DarkGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(list, key = { it.id }) { item ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                Modifier
                                    .padding(top = 6.dp)
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (item.id in newIds) Color(0xFFE53935) else Color.Transparent
                                    )
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    item.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                if (item.body.isNotEmpty()) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        item.body,
                                        color = Color.LightGray,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    dateFormat.format(Date(item.time)),
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        HorizontalDivider(color = Color(0xFF222222))
                    }
                }
            }
        }
    }
}
