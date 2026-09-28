package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.AppId
import com.example.data.model.NotificationModel
import com.example.ui.theme.*

@Composable
fun QuickSettingsShade(
    isOpen: Boolean,
    timeString: String,
    dateString: String,
    isWifi: Boolean,
    isBluetooth: Boolean,
    isFlashlight: Boolean,
    isMuted: Boolean,
    brightness: Float,
    notifications: List<NotificationModel>,
    onToggleWifi: () -> Unit,
    onToggleBluetooth: () -> Unit,
    onToggleFlashlight: () -> Unit,
    onToggleSound: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSPen: () -> Unit,
    onNotificationClick: (AppId?) -> Unit,
    onClearNotifications: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE608090C))
                .clickable { onClose() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF14171F), Color(0xFF1C202B))
                        )
                    )
                    .clickable(enabled = false) {}
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Header: Time & Settings Shortcut
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = timeString,
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dateString,
                            color = Color(0xFF9AA0A6),
                            fontSize = 13.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onOpenSPen,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2B303C))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "S-Pen",
                                tint = SPenGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2B303C))
                                .testTag("quick_settings_settings_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Action Tiles (Samsung One UI style round buttons)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickTileItem(
                        icon = Icons.Default.Wifi,
                        label = "Wi-Fi",
                        isActive = isWifi,
                        onClick = onToggleWifi
                    )
                    QuickTileItem(
                        icon = Icons.Default.Bluetooth,
                        label = "بلوتوث",
                        isActive = isBluetooth,
                        onClick = onToggleBluetooth
                    )
                    QuickTileItem(
                        icon = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        label = if (isMuted) "صامت" else "الصوت",
                        isActive = !isMuted,
                        onClick = onToggleSound
                    )
                    QuickTileItem(
                        icon = Icons.Default.FlashlightOn,
                        label = "المصباح",
                        isActive = isFlashlight,
                        onClick = onToggleFlashlight
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Brightness Slider
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF262A36))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BrightnessMedium,
                        contentDescription = "Brightness",
                        tint = AuraCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = brightness,
                        onValueChange = onBrightnessChange,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = AuraCyan,
                            activeTrackColor = AuraCyan,
                            inactiveTrackColor = Color(0xFF40485C)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notifications Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الإشعارات (${notifications.size})",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (notifications.isNotEmpty()) {
                        Text(
                            text = "مسح الكل",
                            color = SamsungBlueLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onClearNotifications() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Notifications List
                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد إشعارات جديدة",
                            color = Color(0xFF71788E),
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications, key = { it.id }) { item ->
                            NotificationCard(
                                notification = item,
                                onClick = { onNotificationClick(item.targetApp) }
                            )
                        }
                    }
                }

                // Handle bar
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 10.dp)
                        .width(44.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF555D75))
                        .clickable { onClose() }
                )
            }
        }
    }
}

@Composable
fun QuickTileItem(
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
                .background(
                    if (isActive) SamsungBlueLight else Color(0xFF272C38)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.White else Color(0xFF9EABB8),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun NotificationCard(
    notification: NotificationModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF262B37))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SamsungBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = notification.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = notification.timestampFormatted,
                        color = Color(0xFF8C96A8),
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.message,
                    color = Color(0xFFC7CDD9),
                    fontSize = 12.sp,
                    maxLines = 2
                )
            }
        }
    }
}
