package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Note10PunchHole

@Composable
fun StatusBar(
    timeString: String,
    batteryLevel: Int,
    isCharging: Boolean = false,
    isWifi: Boolean = true,
    networkType: String = "5G",
    isMuted: Boolean = false,
    hasNotifications: Boolean = false,
    onStatusClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLightContent: Boolean = true
) {
    val contentColor = if (isLightContent) Color.White else Color.Black.copy(alpha = 0.85f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .clickable { onStatusClick() }
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        // Left Side: Live System Time and Notification indicator
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = timeString,
                color = contentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            if (hasNotifications) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E5FF))
                )
            }
        }

        // Center: Galaxy Note 10 Signature Punch-Hole Camera
        Box(
            modifier = Modifier
                .size(13.dp)
                .clip(CircleShape)
                .background(Note10PunchHole)
                .padding(2.5.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0D1B2A))
            )
        }

        // Right Side: Network, Wifi, Sound, Battery with Charging Bolt
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (isMuted) {
                Icon(
                    imageVector = Icons.Default.VolumeOff,
                    contentDescription = "Muted",
                    tint = contentColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(13.dp)
                )
            }

            if (isWifi) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = "Wi-Fi",
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            Text(
                text = networkType,
                color = contentColor.copy(alpha = 0.9f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )

            Icon(
                imageVector = Icons.Default.SignalCellular4Bar,
                contentDescription = "Cellular",
                tint = contentColor,
                modifier = Modifier.size(13.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$batteryLevel%",
                    color = contentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(2.dp))
                if (isCharging) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "جاري الشحن",
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Icon(
                        imageVector = when {
                            batteryLevel > 80 -> Icons.Default.BatteryFull
                            batteryLevel > 50 -> Icons.Default.Battery6Bar
                            batteryLevel > 20 -> Icons.Default.Battery3Bar
                            else -> Icons.Default.BatteryAlert
                        },
                        contentDescription = "Battery",
                        tint = if (batteryLevel > 20) contentColor else Color(0xFFFF5252),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
