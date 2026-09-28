package com.example.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.PhoneViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicTacToeOnlineApp(viewModel: PhoneViewModel) {
    var board by remember { mutableStateOf(List(9) { "" }) }
    var isPlayerXTurn by remember { mutableStateOf(true) }
    var winner by remember { mutableStateOf<String?>(null) }
    var isVsAi by remember { mutableStateOf(true) }
    var myScore by remember { mutableIntStateOf(0) }
    var opponentScore by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    fun checkWinner(b: List<String>): String? {
        val lines = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )
        for (line in lines) {
            val (a, c, d) = line
            if (b[a].isNotEmpty() && b[a] == b[c] && b[a] == b[d]) {
                return b[a]
            }
        }
        if (b.all { it.isNotEmpty() }) return "DRAW"
        return null
    }

    fun makeMove(index: Int) {
        if (board[index].isNotEmpty() || winner != null) return
        viewModel.vibrate(20)

        val newBoard = board.toMutableList()
        newBoard[index] = if (isPlayerXTurn) "X" else "O"
        board = newBoard

        val w = checkWinner(newBoard)
        if (w != null) {
            winner = w
            if (w == "X") myScore += 1 else if (w == "O") opponentScore += 1
            viewModel.vibrate(70)
        } else {
            isPlayerXTurn = !isPlayerXTurn
            if (isVsAi && !isPlayerXTurn) {
                // AI Move
                coroutineScope.launch {
                    delay(500)
                    val available = board.indices.filter { board[it].isEmpty() }
                    if (available.isNotEmpty()) {
                        val aiIndex = available.random()
                        val aiBoard = board.toMutableList()
                        aiBoard[aiIndex] = "O"
                        board = aiBoard
                        val aiWin = checkWinner(aiBoard)
                        if (aiWin != null) {
                            winner = aiWin
                            if (aiWin == "O") opponentScore += 1
                            viewModel.vibrate(70)
                        } else {
                            isPlayerXTurn = true
                        }
                    }
                }
            }
        }
    }

    fun resetGame() {
        board = List(9) { "" }
        winner = null
        isPlayerXTurn = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SportsEsports, contentDescription = null, tint = Color(0xFF00E5FF))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تحدي XO أونلاين (Multiplayer)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { isVsAi = !isVsAi; resetGame() }) {
                        Icon(
                            if (isVsAi) Icons.Default.SmartToy else Icons.Default.People,
                            contentDescription = "الوضع",
                            tint = Color(0xFF00E5FF)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Score Board
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("أنت (X)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0072DE))
                            Text("$myScore", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }

                        Text("VS", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (isVsAi) "الذكاء الاصطناعي (O)" else "الخصم أونلاين (O)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFFE91E63)
                            )
                            Text("$opponentScore", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Status Banner
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (winner) {
                        "X" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        "O" -> Color(0xFFFF5252).copy(alpha = 0.2f)
                        "DRAW" -> Color(0xFFFF9800).copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = when (winner) {
                            "X" -> "🎉 مبروك! فزت بالجولة"
                            "O" -> "💥 فاز الخصم، حظ أوفر"
                            "DRAW" -> "🤝 تعادل!"
                            else -> if (isPlayerXTurn) "دورك الآن (X) 🎯" else "دور الخصم (O)..."
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = when (winner) {
                            "X" -> Color(0xFF4CAF50)
                            "O" -> Color(0xFFFF5252)
                            "DRAW" -> Color(0xFFFF9800)
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }

            // 3x3 Grid
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    for (row in 0 until 3) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (col in 0 until 3) {
                                val idx = row * 3 + col
                                val cell = board[idx]
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { makeMove(idx) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cell,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (cell == "X") Color(0xFF0072DE) else Color(0xFFE91E63)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Reset / New Match Button
            item {
                Button(
                    onClick = { resetGame() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0072DE)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("جولة جديدة 🔄", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
