package com.example.ui.apps

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.PhoneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrickBreakerApp(viewModel: PhoneViewModel) {
    val gameState by viewModel.brickGame.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SportsEsports, contentDescription = null, tint = Color(0xFF9C27B0))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("كسار الطوب (Brick Breaker)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                actions = {
                    // Lives Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        repeat(3) { index ->
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = if (index < gameState.lives) Color(0xFFE91E63) else Color.Gray.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Score
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF9C27B0).copy(alpha = 0.2f)
                    ) {
                        Text(
                            "النقاط: ${gameState.score}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9C27B0),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0F172A))
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val normalizedX = change.position.x / size.width
                        viewModel.movePaddle(normalizedX)
                    }
                }
        ) {
            // Interactive Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw Bricks
                gameState.bricks.forEach { brick ->
                    if (!brick.isDestroyed) {
                        drawRoundRect(
                            color = brick.color,
                            topLeft = Offset(brick.x * w, brick.y * h),
                            size = Size(brick.width * w, brick.height * h),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                    }
                }

                // Draw Paddle
                val paddleWidth = 0.24f * w
                val paddleHeight = 14.dp.toPx()
                val paddleLeft = (gameState.paddleX * w) - (paddleWidth / 2f)
                val paddleTop = 0.80f * h

                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFF9C27B0))),
                    topLeft = Offset(paddleLeft, paddleTop),
                    size = Size(paddleWidth, paddleHeight),
                    cornerRadius = CornerRadius(12f, 12f)
                )

                // Draw Ball
                val ballRadius = 9.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color(0xFFFFEB3B), Color(0xFFFF9800))),
                    radius = ballRadius,
                    center = Offset(gameState.ballX * w, gameState.ballY * h)
                )
            }

            // Controls & Overlay
            if (!gameState.isPlaying || gameState.isGameOver || gameState.isWon) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = when {
                                    gameState.isWon -> "🎉 مبروك! فزت بالمرحلة"
                                    gameState.isGameOver -> "انتهت المحاولات 💥"
                                    else -> "لعبة كسار الطوب 🕹️"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )

                            Text(
                                "النقاط المحققة: ${gameState.score}",
                                fontSize = 14.sp,
                                color = Color(0xFFFFEB3B)
                            )

                            Text(
                                "اسحب بإصبعك على الشاشة لتحريك المضرب وضرب الكرة لتدمير كافة الطوب.",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )

                            Button(
                                onClick = {
                                    viewModel.startBrickGame()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (gameState.isGameOver || gameState.isWon) "إعادة اللعب 🔄" else "بدء اللعب ▶️",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
