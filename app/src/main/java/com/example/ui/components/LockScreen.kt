package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppId
import com.example.ui.theme.*

@Composable
fun LockScreen(
    timeString: String,
    dateString: String,
    batteryLevel: Int,
    onUnlock: () -> Unit,
    onQuickApp: (AppId) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -30) {
                        onUnlock()
                    }
                }
            }
            .clickable { onUnlock() }
            .padding(horizontal = 24.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Main Clock and Date at Top Center
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = timeString,
                color = Color.White,
                fontSize = 58.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateString,
                color = Color(0xFFE2E8F0),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BatteryFull,
                    contentDescription = null,
                    tint = AuraCyan,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "مشحون بنسبة $batteryLevel%",
                    color = AuraCyan,
                    fontSize = 12.sp
                )
            }
        }

        // Center: Galaxy Note 10 In-Display Fingerprint Sensor Indicator
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Color(0x3300E5FF))
                    .testTag("fingerprint_unlock_sensor")
                    .clickable { onUnlock() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Fingerprint Sensor",
                    tint = AuraCyan,
                    modifier = Modifier.size(38.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "اسحب للأعلى أو اضغط لفتح القفل",
                color = Color(0xCCFFFFFF),
                fontSize = 13.sp
            )
        }

        // Bottom: Quick Shortcuts (Phone & Camera)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quick Phone Shortcut
            IconButton(
                onClick = { onQuickApp(AppId.PHONE) },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0x4D000000))
                    .testTag("lock_phone_shortcut")
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Phone",
                    tint = PhoneGreen,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Swipe Up Pill Indicator
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0x80FFFFFF))
            )

            // Quick Camera Shortcut
            IconButton(
                onClick = { onQuickApp(AppId.CAMERA) },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0x4D000000))
                    .testTag("lock_camera_shortcut")
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Camera",
                    tint = CameraRed,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
