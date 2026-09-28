package com.example.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.VirtualFile
import com.example.ui.theme.*

@Composable
fun FileManagerApp(
    files: List<VirtualFile>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFile by remember { mutableStateOf<VirtualFile?>(null) }
    var currentPath by remember { mutableStateOf("/Storage/Emulated/0") }

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
                    text = "ملفاتي (My Files)",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (selectedFile != null) {
            // Document / File Preview Dialog
            AlertDialog(
                onDismissRequest = { selectedFile = null },
                title = { Text(selectedFile?.name ?: "", color = Color.White, fontSize = 16.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("نوع الملف: ${selectedFile?.mimeType}", color = AuraCyan, fontSize = 12.sp)
                        Text("الحجم: ${selectedFile?.sizeFormatted}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Divider(color = Color(0xFF2C3244))
                        Text(
                            text = selectedFile?.content?.ifBlank { "محتوى الملف الثنائي الافتراضي..." } ?: "",
                            color = Color(0xFFE2E8F0),
                            fontSize = 14.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { selectedFile = null },
                        colors = ButtonDefaults.buttonColors(containerColor = SamsungBlueLight)
                    ) {
                        Text("إغلاق")
                    }
                },
                containerColor = Color(0xFF1E2330)
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Storage Health Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF191D28))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("وحدة التخزين الداخلية", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("42.8 GB / 256 GB", color = AuraCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { 0.17f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = SamsungBlueLight,
                            trackColor = Color(0xFF2B3244),
                        )
                    }
                }
            }

            // Categories Section
            item {
                Text("الفئات", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FileCategoryItem(Icons.Default.Image, "الصور", FilesOrange)
                    FileCategoryItem(Icons.Default.AudioFile, "الصوت", RecorderPink)
                    FileCategoryItem(Icons.Default.Description, "المستندات", SamsungBlueLight)
                    FileCategoryItem(Icons.Default.Download, "التنزيلات", AuraCyan)
                }
            }

            // Files & Folders Section Header
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text("ملفات النظام", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            items(files, key = { it.id }) { file ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { selectedFile = file }
                        .testTag("file_item_${file.id}"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181C26))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
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
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (file.isDirectory) Color(0xFF282115) else Color(0xFF192538)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                                    contentDescription = null,
                                    tint = if (file.isDirectory) FilesOrange else SamsungBlueLight,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = file.name,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${file.sizeFormatted} • ${file.lastModified}",
                                    color = Color(0xFF8892A6),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color(0xFF6E788C),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FileCategoryItem(
    icon: ImageVector,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = Color.White, fontSize = 11.sp)
    }
}
