package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallState
import com.example.ui.theme.*

@Composable
fun InCallOverlay(
    callState: CallState,
    onAnswer: () -> Unit,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!callState.isActive && !callState.isIncoming && !callState.isCalling) return

    val durationFmt = String.format(
        "%02d:%02d",
        callState.durationSeconds / 60,
        callState.durationSeconds % 60
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E1B4B),
                        Color(0xFF020617)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 48.dp)
            ) {
                Text(
                    text = when {
                        callState.isIncoming -> "مكالمة واردة..."
                        callState.isCalling -> "جارٍ الاتصال..."
                        else -> "مكالمة نشطة (HD Voice)"
                    },
                    color = AuraCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = callState.contactName.ifBlank { callState.phoneNumber },
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = callState.phoneNumber,
                    color = Color(0xFF94A3B8),
                    fontSize = 15.sp
                )
                if (callState.isActive) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = durationFmt,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Big Contact Avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(AuraViolet, AuraMagenta)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = callState.contactName.take(1).ifBlank { "📞" },
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Controls & Buttons
            if (callState.isIncoming) {
                // Incoming Call Actions (Green Accept / Red Reject)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 36.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reject
                    CallCircleButton(
                        icon = Icons.Default.CallEnd,
                        label = "رفض",
                        color = Color(0xFFE53935),
                        onClick = onEndCall,
                        testTag = "reject_call_btn"
                    )
                    // Answer
                    CallCircleButton(
                        icon = Icons.Default.Call,
                        label = "رد",
                        color = PhoneGreen,
                        onClick = onAnswer,
                        testTag = "answer_call_btn"
                    )
                }
            } else {
                // Active Call Actions (Mute, Keypad, Speaker, Video, Notes, End)
                Column(
                    modifier = Modifier.padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CallActionButton(
                            icon = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            label = if (callState.isMuted) "كتم مفعل" else "كتم",
                            isActive = callState.isMuted,
                            onClick = onToggleMute
                        )
                        CallActionButton(
                            icon = Icons.Default.Dialpad,
                            label = "لوحة الاتصال",
                            isActive = false,
                            onClick = {}
                        )
                        CallActionButton(
                            icon = if (callState.isSpeaker) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                            label = if (callState.isSpeaker) "مكبر الصوت" else "سماعة الهاتف",
                            isActive = callState.isSpeaker,
                            onClick = onToggleSpeaker
                        )
                    }

                    CallCircleButton(
                        icon = Icons.Default.CallEnd,
                        label = "إنهاء المكالمة",
                        color = Color(0xFFE53935),
                        onClick = onEndCall,
                        size = 64,
                        testTag = "end_call_btn"
                    )
                }
            }
        }
    }
}

@Composable
fun CallCircleButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    size: Int = 60,
    testTag: String = ""
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(color)
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size((size * 0.45).dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}

@Composable
fun CallActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(if (isActive) Color.White else Color(0x33FFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.Black else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = Color(0xFFCBD5E1),
            fontSize = 11.sp
        )
    }
}
