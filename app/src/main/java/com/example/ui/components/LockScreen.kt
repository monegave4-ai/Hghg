package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.AppSettingsEntity
import com.example.data.model.AppId
import com.example.ui.theme.*
import com.example.viewmodel.PhoneViewModel

@Composable
fun LockScreen(
    viewModel: PhoneViewModel,
    timeString: String,
    dateString: String,
    batteryLevel: Int,
    onQuickApp: (AppId) -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settingsState.collectAsState()
    val pinInput by viewModel.pinInput.collectAsState()
    val isFaceScanning by viewModel.isFaceScanning.collectAsState()
    val isFingerprintScanning by viewModel.isFingerprintScanning.collectAsState()
    val lockError by viewModel.lockScreenError.collectAsState()

    var showPinPad by remember { mutableStateOf(false) }

    // Face Scan Animation
    val infiniteTransition = rememberInfiniteTransition(label = "face_scan")
    val scanScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -30) {
                        if (settings.isLockScreenEnabled) {
                            showPinPad = true
                        } else {
                            viewModel.toggleLockScreen(false)
                        }
                    }
                }
            }
            .padding(horizontal = 24.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Top Face Recognition Camera Glow Indicator
        if (settings.isFaceUnlockEnabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
                    .size(if (isFaceScanning) (34 * scanScale).dp else 34.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFaceScanning) Brush.radialGradient(listOf(Color(0xFF00E676), Color.Transparent))
                        else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .clickable { viewModel.triggerFaceUnlock() },
                contentAlignment = Alignment.Center
            ) {
                if (isFaceScanning) {
                    Icon(
                        Icons.Default.Face,
                        contentDescription = "التعرف على الوجه",
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Main Clock & Date
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Face Unlock Prompt
            if (settings.isFaceUnlockEnabled && !showPinPad) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33000000))
                        .clickable { viewModel.triggerFaceUnlock() }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Face,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (isFaceScanning) "جاري التعرف على الوجه..." else "التعرف على الوجه مفعل",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

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
            Spacer(modifier = Modifier.height(10.dp))
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

        // Center Content: Either PIN Keypad or Fingerprint Scanner
        if (showPinPad) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "أدخل رمز PIN",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // PIN Dots
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(4) { index ->
                        val isFilled = index < pinInput.length
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.3f))
                        )
                    }
                }

                if (lockError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(lockError!!, color = Color(0xFFFF5252), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Numeric Keypad Grid
                val keyRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("cancel", "0", "del")
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    keyRows.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            row.forEach { key ->
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x33FFFFFF))
                                        .clickable {
                                            when (key) {
                                                "del" -> viewModel.deletePinDigit()
                                                "cancel" -> showPinPad = false
                                                else -> viewModel.enterPinDigit(key)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (key) {
                                        "del" -> Icon(Icons.Default.Backspace, contentDescription = "حذف", tint = Color.White)
                                        "cancel" -> Text("إلغاء", color = Color.White, fontSize = 11.sp)
                                        else -> Text(key, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Galaxy Note 10 In-Display Ultrasonic Fingerprint Scanner
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .scale(if (isFingerprintScanning) scanScale else 1f)
                        .clip(CircleShape)
                        .background(
                            if (isFingerprintScanning) Brush.radialGradient(listOf(Color(0xFF00E5FF), Color(0x3300E5FF)))
                            else Brush.linearGradient(listOf(Color(0x3300E5FF), Color(0x3300E5FF)))
                        )
                        .testTag("fingerprint_unlock_sensor")
                        .clickable {
                            if (settings.isLockScreenEnabled && settings.isFingerprintEnabled) {
                                viewModel.triggerFingerprintUnlock()
                            } else {
                                viewModel.toggleLockScreen(false)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Fingerprint Sensor",
                        tint = if (isFingerprintScanning) Color.White else AuraCyan,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (settings.isLockScreenEnabled) "المس البصمة أو اسحب للأعلى لكلمة المرور" else "اسحب للأعلى أو اضغط لفتح القفل",
                    color = Color(0xCCFFFFFF),
                    fontSize = 12.sp
                )

                if (settings.isLockScreenEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { showPinPad = true }) {
                        Text("إدخال رمز PIN 🔢", color = AuraCyan, fontSize = 12.sp)
                    }
                }
            }
        }

        // Bottom Shortcuts (Phone & Camera)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0x80FFFFFF))
            )

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
