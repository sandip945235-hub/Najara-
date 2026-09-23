package com.najara.app.ui

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    val context = LocalContext.current

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Privacy Policy", color = Color.White, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text("(यहाँ बाद में प्राइवेसी पॉलिसी जोड़ेंगे)", color = Color.Gray)

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Najara App देखें! 🎬")
                }
                context.startActivity(Intent.createChooser(intent, "Share via"))
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Share App", color = Color.White)
        }
    }
}
