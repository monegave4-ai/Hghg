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
import com.example.ui.theme.*

data class GalleryItem(
    val id: String,
    val title: String,
    val album: String,
    val drawableRes: Int? = null,
    val gradientColors: List<Color> = listOf(AuraViolet, AuraMagenta)
)

@Composable
fun GalleryApp(
    userPhotos: List<String>,
    onSetWallpaper: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Photos, 1: Albums
    var selectedPhoto by remember { mutableStateOf<GalleryItem?>(null) }

    // Pre-loaded Gallery Sample Items + Captured Photos
    val initialItems = remember {
        listOf(
            GalleryItem("w1", "خلفية Aura Glow الرسمية", "الخلفيات", R.drawable.aura_glow_wp_1790583782504),
            GalleryItem("w2", "سلسلة ألوان نوت 10", "الخلفيات", null, listOf(Color(0xFF00C6FF), Color(0xFF0072FF))),
            GalleryItem("s1", "رسمة بقلم S-Pen", "رسومات القلم", null, listOf(Color(0xFFFF9A8B), Color(0xFFFF6A88))),
            GalleryItem("s2", "مخطط تصميم Galaxy", "المستندات", null, listOf(Color(0xFF8EC5FC), Color(0xFFE0C3FC))),
            GalleryItem("c1", "صورة احترافية 4K", "الكاميرا", null, listOf(Color(0xFF43E97B), Color(0xFF38F9D7)))
        )
    }

    val allPhotos = initialItems + userPhotos.map { id ->
        GalleryItem(id, "صورة كاميرا نوت 10", "الكاميرا", null, listOf(AuraViolet, AuraCyan))
    }

    if (selectedPhoto != null) {
        // Full Photo Viewer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Full Image Display
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 50.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedPhoto?.drawableRes != null) {
                    Image(
                        painter = painterResource(id = selectedPhoto!!.drawableRes!!),
                        contentDescription = selectedPhoto?.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .fillMaxHeight(0.75f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Brush.linearGradient(selectedPhoto!!.gradientColors)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(80.dp)
                        )
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
                    text = selectedPhoto?.title ?: "",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.White)
                }
            }

            // Bottom Actions: Set Wallpaper, Share, Edit, Delete
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color(0x80000000))
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        onSetWallpaper("aura_glow")
                        selectedPhoto = null
                    }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Wallpaper, contentDescription = "Set Wallpaper", tint = AuraCyan)
                    }
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit S-Pen", tint = SPenGold)
                }
                IconButton(onClick = { selectedPhoto = null }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252))
                }
            }
        }
    } else {
        // Gallery Gallery Main Grid
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
                        text = "الاستوديو (Gallery)",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Tabs (Pictures, Albums)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                DialerTabItem("الصور (${allPhotos.size})", selectedTab == 0) { selectedTab = 0 }
                Spacer(modifier = Modifier.width(16.dp))
                DialerTabItem("الألبومات", selectedTab == 1) { selectedTab = 1 }
            }

            Divider(color = Color(0xFF222838), modifier = Modifier.padding(top = 8.dp))

            // Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allPhotos, key = { it.id }) { photo ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E2330))
                            .clickable { selectedPhoto = photo }
                            .testTag("gallery_photo_item"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photo.drawableRes != null) {
                            Image(
                                painter = painterResource(id = photo.drawableRes),
                                contentDescription = photo.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.linearGradient(photo.gradientColors)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
