package com.example.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun ClockApp(
    stopwatchMs: Long,
    isStopwatchRunning: Boolean,
    laps: List<Long>,
    onStartStopwatch: () -> Unit,
    onPauseStopwatch: () -> Unit,
    onResetStopwatch: () -> Unit,
    onAddLap: () -> Unit,
    timerRemaining: Int,
    isTimerRunning: Boolean,
    onStartTimer: (Int) -> Unit,
    onStopTimer: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(2) } // 0: Alarm, 1: World, 2: Stopwatch, 3: Timer

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
                    text = "الساعة (Clock)",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Clock Tabs (Alarm, World, Stopwatch, Timer)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DialerTabItem("المنبه", selectedTab == 0) { selectedTab = 0 }
            DialerTabItem("العالمية", selectedTab == 1) { selectedTab = 1 }
            DialerTabItem("ساعة الإيقاف", selectedTab == 2) { selectedTab = 2 }
            DialerTabItem("المؤقت", selectedTab == 3) { selectedTab = 3 }
        }

        Divider(color = Color(0xFF222838), modifier = Modifier.padding(top = 8.dp))

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> AlarmTab()
                1 -> WorldClockTab()
                2 -> StopwatchTab(
                    stopwatchMs = stopwatchMs,
                    isRunning = isStopwatchRunning,
                    laps = laps,
                    onStart = onStartStopwatch,
                    onPause = onPauseStopwatch,
                    onReset = onResetStopwatch,
                    onLap = onAddLap
                )
                3 -> TimerTab(
                    timerRemaining = timerRemaining,
                    isRunning = isTimerRunning,
                    onStartTimer = onStartTimer,
                    onStopTimer = onStopTimer
                )
            }
        }
    }
}

@Composable
fun StopwatchTab(
    stopwatchMs: Long,
    isRunning: Boolean,
    laps: List<Long>,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onLap: () -> Unit
) {
    val totalSec = stopwatchMs / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    val hundredths = (stopwatchMs % 1000) / 10

    val timeStr = String.format("%02d:%02d.%02d", min, sec, hundredths)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Digital Stopwatch Counter
        Box(
            modifier = Modifier
                .padding(top = 40.dp)
                .size(220.dp)
                .clip(CircleShape)
                .background(Color(0xFF181D29)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = timeStr,
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        // Laps List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(laps) { index, lapMs ->
                val lapTotalSec = lapMs / 1000
                val lMin = lapTotalSec / 60
                val lSec = lapTotalSec % 60
                val lHd = (lapMs % 1000) / 10
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E2433))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("دورة ${laps.size - index}", color = AuraCyan, fontSize = 13.sp)
                    Text(String.format("%02d:%02d.%02d", lMin, lSec, lHd), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (isRunning) {
                Button(
                    onClick = onLap,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262E3E)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("دورة (Lap)", color = Color.White)
                }
                Button(
                    onClick = onPause,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("إيقاف مؤقت", color = Color.White)
                }
            } else {
                Button(
                    onClick = onReset,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262E3E)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("إعادة ضبط", color = Color.White)
                }
                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(containerColor = PhoneGreen),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("بدء", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun TimerTab(
    timerRemaining: Int,
    isRunning: Boolean,
    onStartTimer: (Int) -> Unit,
    onStopTimer: () -> Unit
) {
    val min = timerRemaining / 60
    val sec = timerRemaining % 60
    val timeStr = String.format("%02d:%02d", min, sec)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .padding(top = 40.dp)
                .size(200.dp)
                .clip(CircleShape)
                .background(Color(0xFF181D29)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = timeStr,
                color = AuraCyan,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Presets (1 min, 5 min, 10 min, 15 min)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(60 to "1 دقيقة", 300 to "5 دقائق", 600 to "10 دقائق", 900 to "15 دقيقة").forEach { (secVal, label) ->
                Button(
                    onClick = { onStartTimer(secVal) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2433)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(label, fontSize = 11.sp, color = Color.White)
                }
            }
        }

        // Start / Cancel Button
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            if (isRunning) {
                Button(
                    onClick = onStopTimer,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("إلغاء المؤقت", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AlarmTab() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        listOf("06:30 ص" to "صلاة الفجر والعمل", "07:45 ص" to "الاستيقاظ", "02:00 م" to "استراحة الغداء").forEach { (time, desc) ->
            var enabled by remember { mutableStateOf(true) }
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181D29))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(time, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(desc, color = Color(0xFF8892A6), fontSize = 12.sp)
                    }
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
            }
        }
    }
}

@Composable
fun WorldClockTab() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf("الرياض" to "+0 ساعة", "دبي" to "+1 ساعة", "القاهرة" to "-1 ساعة", "مكة المكرمة" to "+0 ساعة", "لندن" to "-3 ساعات").forEach { (city, diff) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181D29))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(city, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(diff, color = Color(0xFF8892A6), fontSize = 12.sp)
                    }
                    Text("12:00 م", color = AuraCyan, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
