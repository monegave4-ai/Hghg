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
import com.example.data.model.AppId
import com.example.data.remote.SupabaseConnectionStatus
import com.example.ui.components.SupabaseConfigDialog
import com.example.ui.theme.*
import com.example.viewmodel.PhoneViewModel

@Composable
fun SettingsApp(
    viewModel: PhoneViewModel,
    settings: AppSettingsEntity,
    onToggleDarkMode: () -> Unit,
    onSelectWallpaper: (String) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showWallpaperDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showWhatsAppUserDialog by remember { mutableStateOf(false) }
    var showSupabaseDialog by remember { mutableStateOf(false) }

    val supabaseStatus by viewModel.supabaseStatus.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "الضبط (Settings)",
                    color = MaterialTheme.colorScheme.onBackground,
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
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
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Galaxy Note 10 • ${settings.currentWhatsAppUsername}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Supabase Cloud Database (سوباباس للرسائل والمتجر)
            item {
                val statusTitle = when (supabaseStatus) {
                    SupabaseConnectionStatus.CONNECTED -> "قاعدة بيانات Supabase 🟢 (متصل بنجاح)"
                    SupabaseConnectionStatus.CONNECTING -> "قاعدة بيانات Supabase 🔵 (جاري الاتصال...)"
                    SupabaseConnectionStatus.ERROR -> "قاعدة بيانات Supabase 🔴 (خطأ بالاتصال)"
                    SupabaseConnectionStatus.NOT_CONFIGURED -> "قاعدة بيانات Supabase 🟡 (غير مهيأ)"
                }
                val statusSubtitle = when (supabaseStatus) {
                    SupabaseConnectionStatus.CONNECTED -> "مزامنة الرسائل الفورية ومتجر التطبيقات مع الأجهزة الأخرى نشطة"
                    SupabaseConnectionStatus.CONNECTING -> "جاري التحقق من الرابط ومفتاح الـ Anon Key..."
                    SupabaseConnectionStatus.ERROR -> "فشل الاتصال - انقر لإعادة فحص الرابط والمفتاح"
                    SupabaseConnectionStatus.NOT_CONFIGURED -> "انقر لإدخال رابط المشروع ومفتاح الـ API لربط السيرفر"
                }

                SettingsCategoryItem(
                    icon = Icons.Default.CloudQueue,
                    title = statusTitle,
                    subtitle = statusSubtitle,
                    iconBg = Color(0xFF3ECF8E),
                    onClick = { showSupabaseDialog = true }
                )
            }

            // Lock Screen & Biometrics (قفل الشاشة والبصمة والوجه)
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Security,
                    title = "شاشة القفل والحماية الحيوية",
                    subtitle = "رمز PIN (${settings.lockPin})، بصمة الإصبع بالموجات فوق الصوتية، والتعرف على الوجه",
                    iconBg = Color(0xFF0072DE),
                    onClick = { showSecurityDialog = true }
                )
            }

            // WhatsApp Account Identity
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Chat,
                    title = "حساب محاكي واتساب",
                    subtitle = "اليوزرنيم الحالي: ${settings.currentWhatsAppUsername} (بدون رقم هاتف)",
                    iconBg = Color(0xFF25D366),
                    onClick = { showWhatsAppUserDialog = true }
                )
            }

            // Device Care Shortcut
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Speed,
                    title = "العناية بالجهاز والبطارية",
                    subtitle = "تحسين الذاكرة والتخزين وحماية Knox",
                    iconBg = Color(0xFF00BCD4),
                    onClick = { viewModel.openApp(AppId.DEVICE_CARE) }
                )
            }

            // Galaxy Store Shortcut
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.ShoppingBag,
                    title = "متجر التطبيقات (Galaxy Store)",
                    subtitle = "تنزيل التطبيقات ونشر حزم الـ APK المجتمعية",
                    iconBg = Color(0xFFE91E63),
                    onClick = { viewModel.openApp(AppId.GALAXY_STORE) }
                )
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
                                Text("الوضع الداكن (Dark Mode)", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("One UI AMOLED Black", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
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

            // S-Pen Air Actions
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Edit,
                    title = "ميزات قلم S-Pen المتقدمة",
                    subtitle = "الإجراءات عن بُعد، أصوات القلم، وقائمة Air Command",
                    iconBg = NotesYellow,
                    onClick = { viewModel.toggleSPenMenu() }
                )
            }

            // Display Brightness Slider
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("سطوع الشاشة (Dynamic AMOLED)", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("${(settings.screenBrightness * 100).toInt()}%", color = Color(0xFF0072DE), fontSize = 12.sp)
                        }
                        Slider(
                            value = settings.screenBrightness,
                            onValueChange = onBrightnessChange,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF0072DE), activeTrackColor = Color(0xFF0072DE))
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

        // Security & Lock Screen Dialog
        if (showSecurityDialog) {
            var lockEnabled by remember { mutableStateOf(settings.isLockScreenEnabled) }
            var pinCode by remember { mutableStateOf(settings.lockPin) }
            var fingerprintEnabled by remember { mutableStateOf(settings.isFingerprintEnabled) }
            var faceEnabled by remember { mutableStateOf(settings.isFaceUnlockEnabled) }

            AlertDialog(
                onDismissRequest = { showSecurityDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF0072DE))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إعدادات قفل الشاشة والحماية", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("تفعيل قفل الشاشة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Switch(checked = lockEnabled, onCheckedChange = { lockEnabled = it })
                        }

                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { if (it.length <= 6) pinCode = it },
                            label = { Text("رمز PIN للقفل") },
                            placeholder = { Text("1234") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("بصمة الإصبع المدمجة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("فتح القفل بلمس الشاشة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = fingerprintEnabled, onCheckedChange = { fingerprintEnabled = it })
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("التعرف على الوجه", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("فتح القفل عبر كاميرا الثقب", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = faceEnabled, onCheckedChange = { faceEnabled = it })
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateSecuritySettings(
                                isLockEnabled = lockEnabled,
                                pin = pinCode.ifBlank { "1234" },
                                isFingerprint = fingerprintEnabled,
                                isFace = faceEnabled
                            )
                            showSecurityDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0072DE))
                    ) {
                        Text("حفظ التغييرات ✅")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSecurityDialog = false }) {
                        Text("إلغاء")
                    }
                }
            )
        }

        // WhatsApp Profile Dialog
        if (showWhatsAppUserDialog) {
            var newUsername by remember { mutableStateOf(settings.currentWhatsAppUsername.removePrefix("@")) }
            var newName by remember { mutableStateOf("مستخدم نوت 10") }

            AlertDialog(
                onDismissRequest = { showWhatsAppUserDialog = false },
                title = { Text("تعديل يوزرنيم الواتساب", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("أدخل اليوزرنيم الخاص بك لتتمكن من مراسلة الآخرين:", fontSize = 12.sp)
                        OutlinedTextField(
                            value = newUsername,
                            onValueChange = { newUsername = it },
                            label = { Text("اليوزرنيم (@)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("الاسم الظاهر") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newUsername.isNotBlank()) {
                                viewModel.registerOrUpdateWhatsAppUsername(newUsername, newName)
                                showWhatsAppUserDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                    ) {
                        Text("حفظ اليوزر")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWhatsAppUserDialog = false }) { Text("إلغاء") }
                }
            )
        }

        // About Phone Dialog
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text("معلومات Samsung Galaxy Note 10", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("اسم الجهاز: Samsung Galaxy Note 10", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("رقم الطراز: SM-N970F/DS (Aura Glow Edition)", fontSize = 12.sp)
                        Text("نظام التشغيل: One UI 5.1 (Android 13)", fontSize = 12.sp)
                        Text("الأمان: Samsung Knox 3.9 & Biometrics", fontSize = 12.sp)
                        Text("الذاكرة: 12 GB RAM / 256 GB UFS 3.0", fontSize = 12.sp)
                        Text("المعالج: Exynos 9825 Octa-Core", fontSize = 12.sp)
                        Text("القلم: S-Pen Bluetooth & 6-Axis Gyro", fontSize = 12.sp, color = Color(0xFFFFB300))
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showAboutDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = SamsungBlueLight)
                    ) {
                        Text("تم")
                    }
                }
            )
        }

        // Wallpaper Picker Dialog
        if (showWallpaperDialog) {
            AlertDialog(
                onDismissRequest = { showWallpaperDialog = false },
                title = { Text("اختر خلفية Note 10") },
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
                    TextButton(onClick = { showWallpaperDialog = false }) { Text("إلغاء") }
                }
            )
        }

        // Supabase Cloud Configuration Dialog
        if (showSupabaseDialog) {
            SupabaseConfigDialog(
                viewModel = viewModel,
                onDismiss = { showSupabaseDialog = false }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
                Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}
