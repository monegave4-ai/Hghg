package com.example.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.StoreAppEntity
import com.example.data.model.AppId
import com.example.data.remote.SupabaseConnectionStatus
import com.example.ui.components.SupabaseConfigDialog
import com.example.viewmodel.PhoneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalaxyStoreApp(viewModel: PhoneViewModel) {
    val storeApps by viewModel.storeApps.collectAsState()
    val supabaseStatus by viewModel.supabaseStatus.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Explore, 1: Community Apps, 2: Publish APK
    var selectedCategory by remember { mutableStateOf("الكل") }
    var searchQuery by remember { mutableStateOf("") }
    var showPublishDialog by remember { mutableStateOf(false) }
    var showSupabaseDialog by remember { mutableStateOf(false) }

    // Publish form state
    var pubName by remember { mutableStateOf("") }
    var pubPkg by remember { mutableStateOf("") }
    var pubDev by remember { mutableStateOf("") }
    var pubCat by remember { mutableStateOf("أدوات") }
    var pubDesc by remember { mutableStateOf("") }
    var pubSize by remember { mutableStateOf("15 MB") }
    var pubColorHex by remember { mutableStateOf("#0072DE") }

    val categories = listOf("الكل", "أدوات", "ألعاب", "تواصل", "إنتاجية", "وسائط")

    val filteredApps = storeApps.filter { app ->
        val matchesCategory = if (selectedCategory == "الكل") true else app.category == selectedCategory
        val matchesTab = when (selectedTab) {
            1 -> app.isCommunityPublished
            else -> true
        }
        val matchesSearch = if (searchQuery.isBlank()) true else {
            app.appName.contains(searchQuery, ignoreCase = true) ||
            app.description.contains(searchQuery, ignoreCase = true) ||
            app.developerName.contains(searchQuery, ignoreCase = true)
        }
        matchesCategory && matchesTab && matchesSearch
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Main Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFE91E63), Color(0xFFFF4081))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Galaxy Store",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "متجر تطبيقات نوت 10 والمجتمع",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = { showSupabaseDialog = true }
                        ) {
                            val cloudTint = when (supabaseStatus) {
                                SupabaseConnectionStatus.CONNECTED -> Color(0xFF3ECF8E)
                                SupabaseConnectionStatus.CONNECTING -> Color(0xFF81D4FA)
                                SupabaseConnectionStatus.ERROR -> Color(0xFFFF8A80)
                                SupabaseConnectionStatus.NOT_CONFIGURED -> Color(0xFFFFD54F)
                            }
                            Icon(Icons.Default.CloudQueue, contentDescription = "سوباباس", tint = cloudTint)
                        }

                        Button(
                            onClick = { showPublishDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0072DE)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("نشر APK", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    placeholder = { Text("بحث عن تطبيقات وألعاب وملفات APK...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "مسح")
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE91E63),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFFE91E63)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("الرئيسية", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        icon = { Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("تطبيقات المجتمع (APK)", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        icon = { Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }

                // Categories Row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedCategory = category },
                            color = if (isSelected) Color(0xFFE91E63) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = category,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Featured Hero Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2640))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF2C3E50), Color(0xFF0072DE), Color(0xFFE91E63))
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("مميز في نوت 10", color = Color(0xFFFFEB3B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFEB3B), modifier = Modifier.size(14.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "متجر تطبيقات ونشر ملفات APK",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "ثبّت التطبيقات وانشر تطبيقاتك المجتمعية لتظهر لجميع مستخدمي الهاتف فوراً.",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (selectedTab == 1) "تطبيقات المجتمع المنشورة (${filteredApps.size})" else "التطبيقات المتاحة (${filteredApps.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Apps List
            if (filteredApps.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("لا توجد تطبيقات مطابقة", color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            } else {
                items(filteredApps) { app ->
                    StoreAppItemCard(
                        app = app,
                        onInstall = { viewModel.installStoreApp(app) },
                        onUninstall = { viewModel.uninstallStoreApp(app.packageName) },
                        onOpen = {
                            when (app.packageName) {
                                "com.whatsapp.note10" -> viewModel.openApp(AppId.WHATSAPP)
                                "com.samsung.photoeditor.spen" -> viewModel.openApp(AppId.PHOTO_EDITOR)
                                "com.arcade.brickbreaker" -> viewModel.openApp(AppId.BRICK_BREAKER)
                                "com.samsung.devicecare" -> viewModel.openApp(AppId.DEVICE_CARE)
                                else -> viewModel.addNotification("تطبيق مجتمعي", "تم فتح ${app.appName} المنشور عبر APK بنجاح!", "النظام")
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Publish APK Dialog
    if (showPublishDialog) {
        AlertDialog(
            onDismissRequest = { showPublishDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF0072DE))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("نشر تطبيق / ملف APK جديد", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("انشر تطبيقك في متجر Galaxy Store ليظهر للجميع ويكون قابلاً للتثبيت والاستخدام:", fontSize = 12.sp)

                    OutlinedTextField(
                        value = pubName,
                        onValueChange = { pubName = it },
                        label = { Text("اسم التطبيق") },
                        placeholder = { Text("مثال: تطبيق الطقس الاحترافي") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = pubPkg,
                        onValueChange = { pubPkg = it },
                        label = { Text("اسم الحزمة (Package Name)") },
                        placeholder = { Text("com.example.myapp") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = pubDev,
                        onValueChange = { pubDev = it },
                        label = { Text("اسم المطور / فريق العمل") },
                        placeholder = { Text("مثال: م. علي المطيري") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = pubDesc,
                        onValueChange = { pubDesc = it },
                        label = { Text("وصف ومزايا التطبيق") },
                        placeholder = { Text("اكتب نبذة عن التطبيق وميزاته...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = pubSize,
                            onValueChange = { pubSize = it },
                            label = { Text("حجم الـ APK") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        var expandedCategory by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = pubCat,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("التصنيف") },
                                trailingIcon = {
                                    IconButton(onClick = { expandedCategory = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = expandedCategory,
                                onDismissRequest = { expandedCategory = false }
                            ) {
                                listOf("أدوات", "ألعاب", "تواصل", "إنتاجية", "وسائط").forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            pubCat = cat
                                            expandedCategory = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Text("اختر لون أيقونة التطبيق:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val colorChoices = listOf(
                            "#0072DE" to Color(0xFF0072DE),
                            "#E91E63" to Color(0xFFE91E63),
                            "#25D366" to Color(0xFF25D366),
                            "#9C27B0" to Color(0xFF9C27B0),
                            "#FF9800" to Color(0xFFFF9800),
                            "#00BCD4" to Color(0xFF00BCD4)
                        )
                        colorChoices.forEach { (hex, col) ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .clickable { pubColorHex = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (pubColorHex == hex) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pubName.isNotBlank()) {
                            viewModel.publishCommunityApp(
                                appName = pubName,
                                packageName = if (pubPkg.isNotBlank()) pubPkg else "com.community.${pubName.lowercase().replace(" ", "_")}",
                                developerName = pubDev,
                                category = pubCat,
                                description = pubDesc,
                                size = pubSize,
                                iconColorHex = pubColorHex
                            )
                            showPublishDialog = false
                            // reset
                            pubName = ""
                            pubPkg = ""
                            pubDesc = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0072DE))
                ) {
                    Text("نشر وتثبيت الآن 🚀")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPublishDialog = false }) {
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
fun StoreAppItemCard(
    app: StoreAppEntity,
    onInstall: () -> Unit,
    onUninstall: () -> Unit,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            val parsedColor = try {
                Color(android.graphics.Color.parseColor(app.iconBgColor))
            } catch (_: Exception) {
                Color(0xFF0072DE)
            }

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(parsedColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (app.iconSymbol) {
                        "whatsapp" -> Icons.Default.Chat
                        "photo_editor" -> Icons.Default.Brush
                        "brick_breaker" -> Icons.Default.SportsEsports
                        "device_care" -> Icons.Default.Security
                        "store" -> Icons.Default.ShoppingBag
                        else -> Icons.Default.Android
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        app.appName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (app.isCommunityPublished) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF0072DE).copy(alpha = 0.2f)
                        ) {
                            Text(
                                "APK",
                                fontSize = 9.sp,
                                color = Color(0xFF0072DE),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Text(
                    app.developerName,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(app.rating.toString(), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                    Text("•", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    Text(app.sizeFormatted, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("•", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    Text(app.category, fontSize = 11.sp, color = Color(0xFFE91E63))
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Button
            if (app.isInstalled) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = onOpen,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("فتح", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (app.packageName != "com.samsung.galaxy.store") {
                        IconButton(
                            onClick = onUninstall,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "إلغاء التثبيت",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            } else {
                Button(
                    onClick = onInstall,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0072DE)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تثبيت", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
