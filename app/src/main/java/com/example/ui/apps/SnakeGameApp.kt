package com.example.ui.apps

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SnakeDirection
import com.example.data.model.SnakeGameState
import com.example.ui.theme.*
import kotlin.math.abs

@Composable
fun SnakeGameApp(
    gameState: SnakeGameState,
    onChangeDirection: (SnakeDirection) -> Unit,
    onRestartGame: () -> Unit,
    onTogglePause: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D14))
    ) {
        // App Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "لعبة الدودة 🐍",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onTogglePause,
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF1E2433))
                ) {
                    Icon(
                        imageVector = if (gameState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onRestartGame,
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF1E2433)).testTag("snake_restart_btn")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = SnakeGreen, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Scoreboard Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151C28)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("النقاط: ", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    Text("${gameState.score}", color = SnakeGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151C28)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("أعلى رقم: ", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    Text("${gameState.highScore}", color = Color(0xFFFFD54F), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Game Arena Canvas (With Touch Swipe Navigation)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(2.dp, Color(0xFF1F293D), RoundedCornerShape(18.dp))
                .background(Color(0xFF0C101A))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val (dx, dy) = dragAmount
                        if (abs(dx) > abs(dy)) {
                            if (dx > 20) onChangeDirection(SnakeDirection.RIGHT)
                            else if (dx < -20) onChangeDirection(SnakeDirection.LEFT)
                        } else {
                            if (dy > 20) onChangeDirection(SnakeDirection.DOWN)
                            else if (dy < -20) onChangeDirection(SnakeDirection.UP)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val grid = gameState.gridSize
                val cellWidth = size.width / grid
                val cellHeight = size.height / grid

                // Draw Food (Apple)
                val foodOffset = Offset(gameState.food.x * cellWidth, gameState.food.y * cellHeight)
                drawRoundRect(
                    color = Color(0xFFFF3366),
                    topLeft = foodOffset.plus(Offset(cellWidth * 0.1f, cellHeight * 0.1f)),
                    size = Size(cellWidth * 0.8f, cellHeight * 0.8f),
                    cornerRadius = CornerRadius(cellWidth * 0.4f, cellHeight * 0.4f)
                )

                // Draw Bonus Food if active
                if (gameState.bonusFood != null) {
                    val bonusOffset = Offset(gameState.bonusFood.x * cellWidth, gameState.bonusFood.y * cellHeight)
                    drawRoundRect(
                        color = Color(0xFFFFD700),
                        topLeft = bonusOffset.plus(Offset(cellWidth * 0.05f, cellHeight * 0.05f)),
                        size = Size(cellWidth * 0.9f, cellHeight * 0.9f),
                        cornerRadius = CornerRadius(cellWidth * 0.3f, cellHeight * 0.3f)
                    )
                }

                // Draw Snake Segments
                gameState.snake.forEachIndexed { index, segment ->
                    val segOffset = Offset(segment.x * cellWidth, segment.y * cellHeight)
                    val isHead = index == 0
                    val segColor = if (isHead) SnakeGreen else Color(0xFF00B0FF).copy(alpha = 0.9f - (index * 0.015f).coerceAtMost(0.4f))

                    drawRoundRect(
                        color = segColor,
                        topLeft = segOffset.plus(Offset(cellWidth * 0.08f, cellHeight * 0.08f)),
                        size = Size(cellWidth * 0.84f, cellHeight * 0.84f),
                        cornerRadius = CornerRadius(if (isHead) cellWidth * 0.4f else cellWidth * 0.2f, if (isHead) cellHeight * 0.4f else cellHeight * 0.2f)
                    )
                }
            }

            // Game Over Overlay
            if (gameState.isGameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xD9000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("انتهت اللعبة! 💥", color = Color(0xFFFF5252), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("مجموع نقاطك: ${gameState.score}", color = Color.White, fontSize = 16.sp)
                        Button(
                            onClick = onRestartGame,
                            colors = ButtonDefaults.buttonColors(containerColor = SnakeGreen),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("game_over_retry_btn")
                        ) {
                            Text("إعادة المحاولة 🔁", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // On-Screen Tactile D-Pad Controller
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // UP
            DPadButton(
                icon = Icons.Default.KeyboardArrowUp,
                onClick = { onChangeDirection(SnakeDirection.UP) },
                testTag = "dpad_up"
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(36.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT
                DPadButton(
                    icon = Icons.Default.KeyboardArrowLeft,
                    onClick = { onChangeDirection(SnakeDirection.LEFT) },
                    testTag = "dpad_left"
                )
                // Center Hub
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1A2233))
                )
                // RIGHT
                DPadButton(
                    icon = Icons.Default.KeyboardArrowRight,
                    onClick = { onChangeDirection(SnakeDirection.RIGHT) },
                    testTag = "dpad_right"
                )
            }

            // DOWN
            DPadButton(
                icon = Icons.Default.KeyboardArrowDown,
                onClick = { onChangeDirection(SnakeDirection.DOWN) },
                testTag = "dpad_down"
            )
        }
    }
}

@Composable
fun DPadButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1E283D))
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}
