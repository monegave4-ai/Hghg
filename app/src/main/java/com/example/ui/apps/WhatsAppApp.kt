package com.example.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.WhatsAppMessageEntity
import com.example.data.local.entities.WhatsAppUserEntity
import com.example.data.remote.SupabaseConnectionStatus
import com.example.ui.components.SupabaseConfigDialog
import com.example.viewmodel.PhoneViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppApp(viewModel: PhoneViewModel) {
    val whatsAppUsers by viewModel.whatsAppUsers.collectAsState()
    val selectedUser by viewModel.selectedWhatsAppUser.collectAsState()
    val settings by viewModel.settingsState.collectAsState()
    val supabaseStatus by viewModel.supabaseStatus.collectAsState()

    var showAddContactDialog by remember { mutableStateOf(false) }
    var showUserSetupDialog by remember { mutableStateOf(false) }
    var showSupabaseDialog by remember { mutableStateOf(false) }

    val whatsAppGreen = Color(0xFF075E54)
    val whatsAppLightGreen = Color(0xFF128C7E)
    val whatsAppAccent = Color(0xFF25D366)

    if (selectedUser != null) {
        WhatsAppConversationScreen(
            viewModel = viewModel,
            user = selectedUser!!,
            onBack = { viewModel.selectedWhatsAppUser.value = null }
        )
    } else {
        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(whatsAppGreen)
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "WhatsApp",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showSupabaseDialog = true }) {
                                val cloudTint = when (supabaseStatus) {
                                    SupabaseConnectionStatus.CONNECTED -> Color(0xFF3ECF8E)
                                    SupabaseConnectionStatus.CONNECTING -> Color(0xFF81D4FA)
                                    SupabaseConnectionStatus.ERROR -> Color(0xFFFF8A80)
                                    SupabaseConnectionStatus.NOT_CONFIGURED -> Color(0xFFFFD54F)
                                }
                                Icon(Icons.Default.CloudQueue, contentDescription = "Supabase Cloud", tint = cloudTint)
                            }
                            IconButton(onClick = { showAddContactDialog = true }) {
                                Icon(Icons.Default.PersonAdd, contentDescription = "إضافة جهة اتصال", tint = Color.White)
                            }
                            IconButton(onClick = { showUserSetupDialog = true }) {
                                Icon(Icons.Default.AccountCircle, contentDescription = "الملف الشخصي", tint = Color.White)
                            }
                        }
                    }

                    // Supabase Cloud Status Strip
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSupabaseDialog = true },
                        color = when (supabaseStatus) {
                            SupabaseConnectionStatus.CONNECTED -> Color(0xFF1B5E20)
                            SupabaseConnectionStatus.CONNECTING -> Color(0xFF0D47A1)
                            SupabaseConnectionStatus.ERROR -> Color(0xFFB71C1C)
                            SupabaseConnectionStatus.NOT_CONFIGURED -> Color(0xFFE65100)
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val cloudText = when (supabaseStatus) {
                                    SupabaseConnectionStatus.CONNECTED -> "سوباباس: 🟢 متصل بالرسائل الحقيقية الفورية"
                                    SupabaseConnectionStatus.CONNECTING -> "سوباباس: 🔵 جاري المزامنة السحابية..."
                                    SupabaseConnectionStatus.ERROR -> "سوباباس: 🔴 خطأ في الاتصال بالسيرفر (انقر للتعديل)"
                                    SupabaseConnectionStatus.NOT_CONFIGURED -> "سوباباس: 🟡 غير مهيأ (انقر لإدخال المفتاح والرابط)"
                                }
                                Text(
                                    text = cloudText,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                "إعدادات ⚙️",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    // User Identity Banner
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showUserSetupDialog = true },
                        color = whatsAppLightGreen
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "حسابك: ${settings.currentWhatsAppUsername}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text("تغيير اليوزر ⚙️", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        }
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddContactDialog = true },
                    containerColor = whatsAppAccent,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "محادثة جديدة")
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (whatsAppUsers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(54.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("لا توجد محادثات حتى الآن", fontWeight = FontWeight.Bold)
                                Text("اضغط على أيقونة المحادثة لإضافة مستخدم باليوزرنيم", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                } else {
                    items(whatsAppUsers) { user ->
                        WhatsAppChatItem(
                            user = user,
                            onClick = { viewModel.selectWhatsAppChat(user) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 72.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }

    // Add Contact Dialog
    if (showAddContactDialog) {
        var newUsername by remember { mutableStateOf("") }
        var newName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = whatsAppLightGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إضافة جهة اتصال باليوزرنيم", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("أدخل اليوزرنيم لأي مستخدم لمراسلته مباشرة:", fontSize = 12.sp)
                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = { newUsername = it },
                        label = { Text("اليوزرنيم (Username)") },
                        placeholder = { Text("@user_ahmed") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("الاسم الظاهر") },
                        placeholder = { Text("أحمد علي") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newUsername.isNotBlank()) {
                            viewModel.addWhatsAppContact(newUsername, newName)
                            showAddContactDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = whatsAppLightGreen)
                ) {
                    Text("بدء المحادثة 💬")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddContactDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // User Setup Dialog
    if (showUserSetupDialog) {
        var myUsername by remember { mutableStateOf(settings.currentWhatsAppUsername.removePrefix("@")) }
        var myDisplayName by remember { mutableStateOf("مستخدم نوت 10") }

        AlertDialog(
            onDismissRequest = { showUserSetupDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = whatsAppLightGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إعداد حساب الواتساب", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("أنشئ أو عدّل حسابك عبر اختيار يوزر نيم فريد بدون رقم هاتف:", fontSize = 12.sp)
                    OutlinedTextField(
                        value = myUsername,
                        onValueChange = { myUsername = it },
                        label = { Text("اليوزرنيم الخاص بك (@)") },
                        placeholder = { Text("my_note10_user") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = myDisplayName,
                        onValueChange = { myDisplayName = it },
                        label = { Text("الاسم الظاهر للجميع") },
                        placeholder = { Text("سلطان نوت 10") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (myUsername.isNotBlank()) {
                            viewModel.registerOrUpdateWhatsAppUsername(myUsername, myDisplayName)
                            showUserSetupDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = whatsAppLightGreen)
                ) {
                    Text("حفظ الحساب ✅")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUserSetupDialog = false }) {
                    Text("إلغاء")
                }
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

@Composable
fun WhatsAppChatItem(
    user: WhatsAppUserEntity,
    onClick: () -> Unit
) {
    val avatarColor = try {
        Color(android.graphics.Color.parseColor(user.avatarColorHex))
    } catch (_: Exception) {
        Color(0xFF25D366)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(avatarColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                user.displayName.take(1),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    user.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (user.isOnline) "متصل" else "اليوم",
                    fontSize = 11.sp,
                    color = if (user.isOnline) Color(0xFF25D366) else MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${user.username} • ${user.statusBio}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppConversationScreen(
    viewModel: PhoneViewModel,
    user: WhatsAppUserEntity,
    onBack: () -> Unit
) {
    val messages by viewModel.getWhatsAppMessagesForUser(user.username).collectAsState(initial = emptyList())
    val storeApps by viewModel.storeApps.collectAsState()
    val listState = rememberLazyListState()

    var messageInput by remember { mutableStateOf("") }
    var showAttachmentSheet by remember { mutableStateOf(false) }

    val whatsAppGreen = Color(0xFF075E54)
    val whatsAppBg = Color(0xFFE5DDD5).copy(alpha = if (MaterialTheme.colorScheme.surface.hashCode() < 0) 0.15f else 0.8f)

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val avatarColor = try {
                            Color(android.graphics.Color.parseColor(user.avatarColorHex))
                        } catch (_: Exception) {
                            Color(0xFF25D366)
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(avatarColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(user.displayName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                user.displayName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                if (user.isOnline) "متصل الآن" else user.lastSeenFormatted,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.startCall(user.displayName, user.username) }) {
                        Icon(Icons.Default.Phone, contentDescription = "اتصال", tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.startCall(user.displayName, user.username) }) {
                        Icon(Icons.Default.Videocam, contentDescription = "فيديو", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = whatsAppGreen)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                // Attachments Picker
                if (showAttachmentSheet) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            // Camera Photo
                            AttachmentOption(
                                icon = Icons.Default.CameraAlt,
                                label = "صورة نوت 10",
                                color = Color(0xFFE91E63),
                                onClick = {
                                    showAttachmentSheet = false
                                    viewModel.sendWhatsAppMessage(
                                        toUsername = user.username,
                                        text = "📸 صورة ملتقطة بكاميرا سامسونج نوت 10 الاحترافية",
                                        mediaType = "IMAGE"
                                    )
                                }
                            )

                            // Voice Audio
                            AttachmentOption(
                                icon = Icons.Default.Mic,
                                label = "تسجيل صوتي",
                                color = Color(0xFFFF9800),
                                onClick = {
                                    showAttachmentSheet = false
                                    viewModel.sendWhatsAppMessage(
                                        toUsername = user.username,
                                        text = "🎙️ رسالة صوتية مسجلة بنقاء عالي (0:15)",
                                        mediaType = "VOICE"
                                    )
                                }
                            )

                            // APK Share
                            AttachmentOption(
                                icon = Icons.Default.Android,
                                label = "تطبيق APK",
                                color = Color(0xFF0072DE),
                                onClick = {
                                    showAttachmentSheet = false
                                    val randomApp = storeApps.firstOrNull { it.isCommunityPublished } ?: storeApps.firstOrNull()
                                    val appName = randomApp?.appName ?: "Flappy S-Pen"
                                    viewModel.sendWhatsAppMessage(
                                        toUsername = user.username,
                                        text = "📦 حزمة تطبيق أندرويد جاهزة للتثبيت: $appName.apk",
                                        mediaType = "APK"
                                    )
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showAttachmentSheet = !showAttachmentSheet }) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = "إرفاق",
                            tint = if (showAttachmentSheet) Color(0xFF0072DE) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        placeholder = { Text("اكتب رسالة...") },
                        shape = RoundedCornerShape(24.dp),
                        singleLine = false,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF128C7E),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    IconButton(
                        onClick = {
                            if (messageInput.isNotBlank()) {
                                viewModel.sendWhatsAppMessage(
                                    toUsername = user.username,
                                    text = messageInput.trim()
                                )
                                messageInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF128C7E))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(whatsAppBg)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                WhatsAppMessageBubble(msg = msg)
            }
        }
    }
}

@Composable
fun AttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun WhatsAppMessageBubble(msg: WhatsAppMessageEntity) {
    val isMe = msg.isFromMe
    val bubbleColor = if (isMe) Color(0xFFDCF8C6) else Color(0xFFFFFFFF)
    val textColor = Color(0xFF111111)

    val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = timeFmt.format(Date(msg.timestamp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isMe) 14.dp else 2.dp,
                bottomEnd = if (isMe) 2.dp else 14.dp
            ),
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                // Media Indicator
                if (msg.mediaType != "TEXT") {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.08f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                when (msg.mediaType) {
                                    "IMAGE" -> Icons.Default.Image
                                    "VOICE" -> Icons.Default.Mic
                                    "APK" -> Icons.Default.Android
                                    else -> Icons.Default.Description
                                },
                                contentDescription = null,
                                tint = Color(0xFF075E54),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                when (msg.mediaType) {
                                    "IMAGE" -> "صورة جالاكسي نوت 10"
                                    "VOICE" -> "تسجيل صوتي HD"
                                    "APK" -> "حزمة APK أندرويد"
                                    else -> "ملف مرفق"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF075E54)
                            )
                        }
                    }
                }

                Text(
                    text = msg.text,
                    fontSize = 14.sp,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        timeStr,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.DoneAll,
                            contentDescription = "مقروءة",
                            tint = Color(0xFF34B7F1),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
