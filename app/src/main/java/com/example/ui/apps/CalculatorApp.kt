package com.example.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CalculatorApp(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    fun calculate() {
        if (expression.isBlank()) return
        try {
            // Simple robust math evaluation for + - × ÷ %
            val sanitized = expression
                .replace("×", "*")
                .replace("÷", "/")
                .replace("−", "-")
            val res = evaluateSimpleMath(sanitized)
            result = if (res % 1.0 == 0.0) res.toLong().toString() else String.format("%.2f", res)
        } catch (_: Exception) {
            result = "خطأ"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F121A))
    ) {
        // App Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "الآلة الحاسبة",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Display Area (Expression & Result)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = expression.ifBlank { "0" },
                color = if (result.isBlank()) Color.White else Color(0xFF94A3B8),
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (result.isNotBlank()) {
                Text(
                    text = result,
                    color = Color(0xFF22C55E),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Divider(color = Color(0xFF222838))

        // Calculator Buttons Matrix (One UI Green/Orange/Dark Gray accents)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val rows = listOf(
                listOf("C" to Color(0xFFEF4444), "()" to Color(0xFF10B981), "%" to Color(0xFF10B981), "÷" to Color(0xFF10B981)),
                listOf("7" to Color(0xFF1E2330), "8" to Color(0xFF1E2330), "9" to Color(0xFF1E2330), "×" to Color(0xFF10B981)),
                listOf("4" to Color(0xFF1E2330), "5" to Color(0xFF1E2330), "6" to Color(0xFF1E2330), "−" to Color(0xFF10B981)),
                listOf("1" to Color(0xFF1E2330), "2" to Color(0xFF1E2330), "3" to Color(0xFF1E2330), "+" to Color(0xFF10B981)),
                listOf("+/-" to Color(0xFF1E2330), "0" to Color(0xFF1E2330), "." to Color(0xFF1E2330), "=" to Color(0xFF10B981))
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { (label, bg) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.2f)
                                .clip(CircleShape)
                                .background(bg)
                                .clickable {
                                    when (label) {
                                        "C" -> {
                                            expression = ""
                                            result = ""
                                        }
                                        "=" -> calculate()
                                        "()" -> {
                                            expression += if (expression.count { it == '(' } > expression.count { it == ')' }) ")" else "("
                                        }
                                        "+/-" -> {
                                            if (expression.startsWith("-")) expression = expression.drop(1)
                                            else expression = "-$expression"
                                        }
                                        else -> {
                                            expression += label
                                        }
                                    }
                                }
                                .testTag("calc_btn_$label"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun evaluateSimpleMath(str: String): Double {
    // Simple arithmetic parser for expressions
    val tokens = mutableListOf<String>()
    var current = StringBuilder()
    for (char in str) {
        if (char in "+-*/%") {
            if (current.isNotEmpty()) {
                tokens.add(current.toString())
                current = StringBuilder()
            }
            tokens.add(char.toString())
        } else {
            current.append(char)
        }
    }
    if (current.isNotEmpty()) tokens.add(current.toString())

    if (tokens.isEmpty()) return 0.0
    var value = tokens[0].toDoubleOrNull() ?: 0.0
    var i = 1
    while (i < tokens.size) {
        val op = tokens[i]
        val nextVal = tokens.getOrNull(i + 1)?.toDoubleOrNull() ?: 0.0
        when (op) {
            "+" -> value += nextVal
            "-" -> value -= nextVal
            "*" -> value *= nextVal
            "/" -> if (nextVal != 0.0) value /= nextVal
            "%" -> value %= nextVal
        }
        i += 2
    }
    return value
}
