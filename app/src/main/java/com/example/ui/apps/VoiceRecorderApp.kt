package com.example.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.RecordingEntity
import com.example.ui.theme.*

@Composable
fun VoiceRecorderApp(
    isRecording: Boolean,
    isPaused: Boolean,
    durationSeconds: Int,
    amplitudes: List<Float>,
    recordings: List<RecordingEntity>,
    onStartRecording: () -> Unit,
    onPauseRecording: () -> Unit,
    onStopAndSaveRecording: (String) -> Unit,
    onDeleteRecording: (RecordingEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRecordingsList by remember { mutableStateOf(false) }
    var recordingName by remember { mutableStateOf("") }
    var currentlyPlayingId by remember { mutableStateOf<Long?>(null) }

    val formattedTime = String.format(
        "%02d:%02d:%02d",
        durationSeconds / 3600,
        (durationSeconds % 3600) / 60,
        durationSeconds % 60
    )

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
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "مسجل الصوت",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Toggle List of Recordings
            IconButton(
                onClick = { showRecordingsList = !showRecordingsList },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF222736))
                    .testTag("recorder_list_toggle")
            ) {
                Icon(
                    imageVector = if (showRecordingsList) Icons.Default.Mic else Icons.Default.List,
                    contentDescription = "Recordings List",
                    tint = if (showRecordingsList) RecorderPink else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (showRecordingsList) {
            // Recordings Library List
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "التسجيلات المحفوظة (${recordings.size})",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (recordings.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد تسجيلات صوتية بعد",
                            color = Color(0xFF7E8A9E),
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(recordings, key = { it.id }) { rec ->
                            val isPlaying = currentlyPlayingId == rec.id
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF191E2A))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                currentlyPlayingId = if (isPlaying) null else rec.id
                                            },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(if (isPlaying) RecorderPink else Color(0xFF2B3244))
                                        ) {
                                            Icon(
                                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = "Play",
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = rec.title,
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${rec.durationSeconds} ثانية • ${rec.fileSizeFormatted}",
                                                color = Color(0xFF8892A6),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { onDeleteRecording(rec) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFF8892A6),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Main Live Recorder Screen
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Recording Mode Pills (Standard / Interview / Voice Memo)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "قياسي (Standard)",
                        color = RecorderPink,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF261D28))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                    Text(
                        text = "مقابلة (Interview)",
                        color = Color(0xFF8E99A8),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }

                // Waveform Audio Frequency Visualizer Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF161A24))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isRecording) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val displayAmps = if (amplitudes.isEmpty()) List(28) { 0.2f } else amplitudes
                            displayAmps.takeLast(28).forEach { amp ->
                                val barHeight = (amp * 130).coerceIn(8f, 140f)
                                Box(
                                    modifier = Modifier
                                        .width(5.dp)
                                        .height(barHeight.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(RecorderPink, AuraMagenta, AuraCyan)
                                            )
                                        )
                                )
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "اضغط على الزر الأحمر لبدء التسجيل",
                                color = Color(0xFF7E8A9E),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Time Duration Display
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formattedTime,
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    if (isRecording) {
                        Text(
                            text = if (isPaused) "التسجيل متوقف مؤقتاً" else "جارٍ التسجيل المباشر 🔴",
                            color = if (isPaused) Color(0xFFFFD54F) else RecorderPink,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Bottom Recorder Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRecording) {
                        // Pause / Resume Button
                        IconButton(
                            onClick = onPauseRecording,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF252B3C))
                        ) {
                            Icon(
                                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = "Pause/Resume",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Big Stop & Save Button
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(RecorderPink)
                                .clickable {
                                    val name = "تسجيل_${System.currentTimeMillis() % 10000}"
                                    onStopAndSaveRecording(name)
                                }
                                .testTag("recorder_stop_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White)
                            )
                        }

                        // Cancel / Discard
                        IconButton(
                            onClick = { onStopAndSaveRecording("تسجيل_سريع") },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF252B3C))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save",
                                tint = PhoneGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        // Big Start Recording Red Button
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .clip(CircleShape)
                                .background(RecorderPink)
                                .clickable { onStartRecording() }
                                .testTag("recorder_start_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Start Recording",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
