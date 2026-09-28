package com.example.ui.apps

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.GalleryPhotoEntity
import com.example.data.model.AppId
import com.example.ui.theme.*
import com.example.viewmodel.PhoneViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GalleryApp(
    viewModel: PhoneViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activePhotos by viewModel.activeGalleryPhotos.collectAsState()
    val trashPhotos by viewModel.trashGalleryPhotos.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Pictures, 1: Albums, 2: Trash
    var selectedPhoto by remember { mutableStateOf<GalleryPhotoEntity?>(null) }
    var selectedAlbumFilter by remember { mutableStateOf<String?>("الكل") }

    if (selectedPhoto != null) {
        val photo = selectedPhoto!!
        val isTrash = photo.isDeleted

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Main Image Box
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                if (photo.uriOrResId == "note10_wallpaper") {
                    Image(
                        painter = painterResource(id = R.drawable.aura_glow_wp_1790583782504),
                        contentDescription = photo.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .fillMaxHeight(0.78f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    when {
                                        photo.isCameraCaptured -> listOf(Color(0xFF0072DE), Color(0xFF00C6FF), Color(0xFFE91E63))
                                        photo.filterApplied == "VINTAGE" -> listOf(Color(0xFFD7CCC8), Color(0xFF5D4037))
                                        photo.filterApplied == "CYBERPUNK" -> listOf(Color(0xFF00E5FF), Color(0xFFE040FB))
                                        else -> listOf(Color(0xFF8EC5FC), Color(0xFFE0C3FC))
                                    }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (photo.isCameraCaptured) Icons.Default.CameraAlt else Icons.Default.Brush,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(photo.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                "ملتقطة بكاميرا سامسونج نوت 10 بدقة عالية",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Top Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color(0x66000000))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedPhoto = null }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = photo.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.White)
                }
            }

            // Bottom Actions Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color(0x99000000))
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isTrash) {
                    // In Trash: Restore or Delete Forever
                    Button(
                        onClick = {
                            viewModel.restorePhotoFromTrash(photo.id)
                            selectedPhoto = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("استعادة الصورة", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.permanentlyDeletePhoto(photo.id)
                            selectedPhoto = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حذف نهائي", fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Active photo: Set Wallpaper, Edit S-Pen, Move to Trash
                    IconButton(
                        onClick = {
                            viewModel.updateWallpaper("aura_glow")
                            selectedPhoto = null
                            viewModel.vibrate(30)
                        }
                    ) {
                        Icon(Icons.Default.Wallpaper, contentDescription = "Set Wallpaper", tint = AuraCyan)
                    }

                    IconButton(
                        onClick = {
                            selectedPhoto = null
                            viewModel.openApp(AppId.PHOTO_EDITOR)
                        }
                    ) {
                        Icon(Icons.Default.Brush, contentDescription = "تعديل بقلم S-Pen", tint = Color(0xFFFF9800))
                    }

                    IconButton(
                        onClick = {
                            viewModel.sendWhatsAppMessage("@samsung_ai", "📸 أشارك معك صورة من استوديو نوت 10", "IMAGE")
                            viewModel.openApp(AppId.WHATSAPP)
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF25D366))
                    }

                    IconButton(
                        onClick = {
                            viewModel.movePhotoToTrash(photo.id)
                            selectedPhoto = null
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "نقل لسلة المحذوفات", tint = Color(0xFFFF5252))
                    }
                }
            }
        }
    } else {
        // Main Gallery Screen
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "الاستوديو (Gallery)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                if (selectedTab == 2 && trashPhotos.isNotEmpty()) {
                    TextButton(onClick = { viewModel.emptyTrash() }) {
                        Text("تفريغ السلة 🗑️", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF9C27B0)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("الصور (${activePhotos.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("الألبومات", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("سلة المحذوفات (${trashPhotos.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTab) {
                0 -> {
                    // Pictures Grid
                    if (activePhotos.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("لا توجد صور حالياً", fontWeight = FontWeight.Bold)
                                Text("التقط صوراً بالكاميرا أو ارسم بـ S-Pen لتظهر هنا", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(activePhotos, key = { it.id }) { photo ->
                                GalleryPhotoGridItem(
                                    photo = photo,
                                    onClick = { selectedPhoto = photo }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Albums View
                    val cameraPhotos = activePhotos.filter { it.isCameraCaptured }
                    val drawingPhotos = activePhotos.filter { !it.isCameraCaptured }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AlbumCardItem(
                            title = "الكاميرا (Camera)",
                            count = cameraPhotos.size,
                            icon = Icons.Default.CameraAlt,
                            gradient = listOf(Color(0xFF0072DE), Color(0xFF00C6FF)),
                            onClick = { selectedTab = 0 }
                        )

                        AlbumCardItem(
                            title = "رسومات S-Pen والمعدلة",
                            count = drawingPhotos.size,
                            icon = Icons.Default.Brush,
                            gradient = listOf(Color(0xFFFF9800), Color(0xFFFF5722)),
                            onClick = { selectedTab = 0 }
                        )

                        AlbumCardItem(
                            title = "الخلفيات الرسمية Note 10",
                            count = 2,
                            icon = Icons.Default.Wallpaper,
                            gradient = listOf(AuraViolet, AuraMagenta),
                            onClick = { selectedTab = 0 }
                        )
                    }
                }
                2 -> {
                    // Trash / Recycle Bin
                    if (trashPhotos.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(52.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("سلة المحذوفات فارغة", fontWeight = FontWeight.Bold)
                                Text("الصور المحذوفة ستبقى هنا 30 يوماً قبل حذفها نهائياً.", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("اضغط على أي صورة لاستعادتها أو حذفها نهائياً", fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(trashPhotos, key = { it.id }) { photo ->
                                    GalleryPhotoGridItem(
                                        photo = photo,
                                        onClick = { selectedPhoto = photo }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GalleryPhotoGridItem(
    photo: GalleryPhotoEntity,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .testTag("gallery_photo_item"),
        contentAlignment = Alignment.Center
    ) {
        if (photo.uriOrResId == "note10_wallpaper") {
            Image(
                painter = painterResource(id = R.drawable.aura_glow_wp_1790583782504),
                contentDescription = photo.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            if (photo.isCameraCaptured) listOf(Color(0xFF0072DE), Color(0xFF00C6FF))
                            else listOf(Color(0xFFFF9800), Color(0xFFE91E63))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (photo.isCameraCaptured) Icons.Default.CameraAlt else Icons.Default.Brush,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Camera / S-Pen Badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(4.dp)
        ) {
            Icon(
                imageVector = if (photo.isCameraCaptured) Icons.Default.CameraAlt else Icons.Default.Brush,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(10.dp)
            )
        }
    }
}

@Composable
fun AlbumCardItem(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(gradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("$count صورة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
