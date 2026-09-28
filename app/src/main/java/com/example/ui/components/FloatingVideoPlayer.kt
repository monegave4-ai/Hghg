package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun FloatingVideoPlayer(
    isOpen: Boolean,
    videoTitle: String = "Samsung Galaxy Note 10 - Official Cinematic Film 4K",
    onClose: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    var offsetX by remember { mutableFloatStateOf(40f) }
    var offsetY by remember { mutableFloatStateOf(160f) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentProgress by remember { mutableFloatStateOf(0.35f) }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1000)
            currentProgress = if (currentProgress >= 1f) 0f else currentProgress + 0.015f
        }
    }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX = (offsetX + dragAmount.x).coerceIn(0f, 400f)
                    offsetY = (offsetY + dragAmount.y).coerceIn(0f, 1000f)
                }
            }
            .width(260.dp)
            .height(170.dp)
            .shadow(16.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A))
    ) {
        // Video Preview Simulated View
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF1E293B), Color(0xFF0072DE), Color(0xFFE91E63))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(
                    Icons.Default.PlayCircleFilled,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { isPlaying = !isPlaying }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    videoTitle,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    "شاشة منبثقة عائمة (Picture-in-Picture)",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 9.sp
                )
            }
        }

        // Top Controls Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x80000000))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF3B30))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("فيديو عائم Note 10", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Row {
                IconButton(onClick = onExpand, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Fullscreen, contentDescription = "تكبير", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Bottom Progress Bar
        LinearProgressIndicator(
            progress = { currentProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .align(Alignment.BottomCenter),
            color = Color(0xFF00E5FF),
            trackColor = Color.White.copy(alpha = 0.2f)
        )
    }
}
