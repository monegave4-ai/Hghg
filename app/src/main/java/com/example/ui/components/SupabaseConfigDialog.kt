package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.remote.SupabaseConnectionStatus
import com.example.data.remote.SupabaseManager
import com.example.viewmodel.PhoneViewModel

@Composable
fun SupabaseConfigDialog(
    viewModel: PhoneViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentConfig = remember { viewModel.supabaseManager.getConfig() }
    var urlText by remember { mutableStateOf(currentConfig.url) }
    var keyText by remember { mutableStateOf(currentConfig.anonKey) }

    val connectionStatus by viewModel.supabaseStatus.collectAsState()
    val statusMessage by viewModel.supabaseStatusMessage.collectAsState()
    var isTesting by remember { mutableStateOf(false) }
    var showSqlCode by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 16.dp)
                .testTag("supabase_config_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF3ECF8E), Color(0xFF1E824C))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudQueue,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "قاعدة بيانات سوباباس (Supabase)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "الرسائل الفورية الحقيقية والمتجر السحابي",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Connection Status Card
                val statusBg = when (connectionStatus) {
                    SupabaseConnectionStatus.CONNECTED -> Color(0xFF1B5E20).copy(alpha = 0.2f)
                    SupabaseConnectionStatus.CONNECTING -> Color(0xFF0D47A1).copy(alpha = 0.2f)
                    SupabaseConnectionStatus.ERROR -> Color(0xFFB71C1C).copy(alpha = 0.2f)
                    SupabaseConnectionStatus.NOT_CONFIGURED -> Color(0xFFE65100).copy(alpha = 0.15f)
                }

                val statusBorder = when (connectionStatus) {
                    SupabaseConnectionStatus.CONNECTED -> Color(0xFF3ECF8E)
                    SupabaseConnectionStatus.CONNECTING -> Color(0xFF29B6F6)
                    SupabaseConnectionStatus.ERROR -> Color(0xFFEF5350)
                    SupabaseConnectionStatus.NOT_CONFIGURED -> Color(0xFFFFA726)
                }

                val statusText = when (connectionStatus) {
                    SupabaseConnectionStatus.CONNECTED -> "🟢 متصل بسوباباس بنجاح"
                    SupabaseConnectionStatus.CONNECTING -> "🔵 جاري اختبار الاتصال..."
                    SupabaseConnectionStatus.ERROR -> "🔴 خطأ في الاتصال بسوباباس"
                    SupabaseConnectionStatus.NOT_CONFIGURED -> "🟡 غير مهيأ - يرجى إدخال الرابط والمفتاح"
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = statusBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(statusBorder, statusBorder)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(statusBorder)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = statusText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (statusMessage.isNotBlank()) {
                                Text(
                                    text = statusMessage,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Supabase URL Input
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "رابط المشروع (Supabase Project URL):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    val clip = clipboard?.primaryClip
                                    if (clip != null && clip.itemCount > 0) {
                                        urlText = clip.getItemAt(0).text.toString().trim()
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("لصق", fontSize = 12.sp)
                            }
                        }

                        OutlinedTextField(
                            value = urlText,
                            onValueChange = { urlText = it },
                            placeholder = { Text("https://xxxxxx.supabase.co", fontSize = 13.sp) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFF3ECF8E))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("supabase_url_input"),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    // Supabase Anon Key Input
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "مفتاح الوصول العام (Anon Public API Key):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    val clip = clipboard?.primaryClip
                                    if (clip != null && clip.itemCount > 0) {
                                        keyText = clip.getItemAt(0).text.toString().trim()
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("لصق", fontSize = 12.sp)
                            }
                        }

                        OutlinedTextField(
                            value = keyText,
                            onValueChange = { keyText = it },
                            placeholder = { Text("eyJhbGciOiJIUzI1NiIsInR5cCI6...", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF3ECF8E))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("supabase_key_input"),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    // Action Buttons (Save & Test, Sync, Clear)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                isTesting = true
                                viewModel.saveAndTestSupabase(urlText, keyText) {
                                    isTesting = false
                                }
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("supabase_save_btn"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3ECF8E),
                                contentColor = Color.Black
                            )
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("جاري الفحص...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("حفظ واختبار الاتصال", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.syncSupabaseNow()
                                Toast.makeText(context, "جاري مزامنة الرسائل والمتجر...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("supabase_sync_btn"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مزامنة فورية", fontSize = 12.sp)
                        }
                    }

                    // Clear button
                    if (currentConfig.isConfigured) {
                        TextButton(
                            onClick = {
                                urlText = ""
                                keyText = ""
                                viewModel.clearSupabaseConfig()
                                Toast.makeText(context, "تم مسح إعدادات Supabase", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("مسح الإعدادات والعودة للوضع المحلي", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }

                    // SQL Schema Instructions & Copy Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DataObject,
                                        contentDescription = null,
                                        tint = Color(0xFF3ECF8E),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "كود إنشاء الجداول في Supabase",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        val clip = ClipData.newPlainText("Supabase SQL Schema", SupabaseManager.SQL_SCHEMA_SCRIPT)
                                        clipboard?.setPrimaryClip(clip)
                                        Toast.makeText(context, "📋 تم نسخ كود SQL! الصقه في Supabase SQL Editor واضغط Run", Toast.LENGTH_LONG).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF3ECF8E).copy(alpha = 0.2f),
                                        contentColor = Color(0xFF3ECF8E)
                                    )
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("نسخ كود SQL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "1. افتح لوحة تحكم Supabase > SQL Editor\n2. الصق الكود واضغط Run لإنشاء جداول messages و store_apps و whatsapp_users فوراً.\n3. انسخ Project URL و anon key من إعدادات API وألصقهما بالأعلى.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(
                                onClick = { showSqlCode = !showSqlCode },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = if (showSqlCode) "إخفاء كود SQL ▲" else "عرض كود SQL بالتفصيل ▼",
                                    fontSize = 11.sp,
                                    color = Color(0xFF3ECF8E)
                                )
                            }

                            if (showSqlCode) {
                                SelectionContainer {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF1E1E1E))
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = SupabaseManager.SQL_SCHEMA_SCRIPT,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = Color(0xFF80CBC4),
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Done Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text("تم الإغلاق", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
