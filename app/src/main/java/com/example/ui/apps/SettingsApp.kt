package com.example.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.AppSettingsEntity
import com.example.ui.theme.*

@Composable
fun SettingsApp(
    settings: AppSettingsEntity,
    onToggleDarkMode: () -> Unit,
    onToggleLockScreen: () -> Unit,
    onSelectWallpaper: (String) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showWallpaperDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

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
                    text = "الضبط (Settings)",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Samsung Account Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF191D28))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(SamsungBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "حساب Samsung",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Galaxy Note 10 • monegave4@gmail.com",
                                color = Color(0xFF8892A6),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Wallpapers Setting
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Wallpaper,
                    title = "الخلفية والنمط",
                    subtitle = "خلفيات سامسونج الرسمية وخلفية Aura Glow",
                    iconBg = GalleryPurple,
                    onClick = { showWallpaperDialog = true }
                )
            }

            // Dark Mode Setting
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181C26))
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
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2C3244)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DarkMode,
                                    contentDescription = null,
                                    tint = AuraCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text("الوضع الداكن (Dark Mode)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("One UI AMOLED Black", color = Color(0xFF8892A6), fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = settings.isDarkMode,
                            onCheckedChange = { onToggleDarkMode() },
                            modifier = Modifier.testTag("settings_dark_mode_switch")
                        )
                    }
                }
            }

            // Lock Screen Setting
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181C26))
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
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2A2038)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = AuraMagenta,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text("شاشة القفل (Lock Screen)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("Always On Display والبصمة", color = Color(0xFF8892A6), fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = settings.isLockScreenEnabled,
                            onCheckedChange = { onToggleLockScreen() },
                            modifier = Modifier.testTag("settings_lockscreen_switch")
                        )
                    }
                }
            }

            // S-Pen Air Actions
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Edit,
                    title = "ميزات قلم S-Pen المتقدمة",
                    subtitle = "الإجراءات عن بُعد، أصوات القلم، وقائمة Air Command",
                    iconBg = NotesYellow,
                    onClick = {}
                )
            }

            // Display Brightness Slider
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181C26))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("سطوع الشاشة (Dynamic AMOLED)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("${(settings.screenBrightness * 100).toInt()}%", color = AuraCyan, fontSize = 12.sp)
                        }
                        Slider(
                            value = settings.screenBrightness,
                            onValueChange = onBrightnessChange,
                            colors = SliderDefaults.colors(thumbColor = AuraCyan, activeTrackColor = AuraCyan)
                        )
                    }
                }
            }

            // About Phone (Galaxy Note 10 Specs)
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.PhoneAndroid,
                    title = "حول الهاتف (About Phone)",
                    subtitle = "Galaxy Note 10 • SM-N970F • One UI 5.1",
                    iconBg = SamsungBlueLight,
                    onClick = { showAboutDialog = true }
                )
            }
        }

        // About Phone Dialog
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text("معلومات Samsung Galaxy Note 10", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("اسم الجهاز: Galaxy Note 10", color = Color.White, fontSize = 13.sp)
                        Text("رقم الطراز: SM-N970F/DS", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("إصدار One UI: 5.1", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("إصدار Android: 13 (Tiramisu)", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("الذاكرة العشوائية: 8 GB RAM", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("سعة التخزين: 256 GB UFS 3.0", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("الشاشة: 6.3 بوصة Dynamic AMOLED HDR10+", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("القلم: S-Pen مع بلوتوث ومستشعر حركي 6 محاور", color = SPenGold, fontSize = 12.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showAboutDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = SamsungBlueLight)
                    ) {
                        Text("تم")
                    }
                },
                containerColor = Color(0xFF1E2330)
            )
        }

        // Wallpaper Picker Dialog
        if (showWallpaperDialog) {
            AlertDialog(
                onDismissRequest = { showWallpaperDialog = false },
                title = { Text("اختر خلفية Note 10", color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                onSelectWallpaper("aura_glow")
                                showWallpaperDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Aura Glow (الخلفية الرسمية)")
                        }
                        Button(
                            onClick = {
                                onSelectWallpaper("aura_black")
                                showWallpaperDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Aura Black (الأسود الليلي)")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showWallpaperDialog = false }) { Text("إلغاء", color = Color.White) }
                },
                containerColor = Color(0xFF1E2330)
            )
        }
    }
}

@Composable
fun SettingsCategoryItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconBg: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181C26))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(iconBg.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconBg,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = Color(0xFF8892A6), fontSize = 11.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF64748B))
        }
    }
}
