package com.najara.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.najara.app.data.ShareHelper

@Composable
fun ShareDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {

                // ===== TOP: RED HEADER =====
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE53935))
                        .padding(horizontal = 16.dp, vertical = 18.dp)
                ) {
                    // Close Button (X)
                    Text(
                        "✕",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clickable { onDismiss() }
                    )

                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(end = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Sharing is Caring! Please share Najara with atleast one friend to help us grow",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "🙏",
                            fontSize = 30.sp
                        )
                    }
                }

                // ===== MIDDLE: INFO =====
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEEEEEE))
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📦 Najara.apk फ़ाइल शेयर होगी",
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                }

                Spacer(Modifier.height(8.dp))

                // ===== BOTTOM: SHARE BUTTON =====
                Button(
                    onClick = {
                        ShareHelper.shareApk(context)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(50.dp)
                ) {
                    Text(
                        "Share Now",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
