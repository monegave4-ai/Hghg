package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppId
import com.example.data.model.AppItem
import com.example.data.model.WeatherData
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    apps: List<AppItem>,
    weather: WeatherData,
    timeString: String,
    dateString: String,
    onOpenApp: (AppId) -> Unit,
    onOpenAppDrawer: () -> Unit,
    onOpenWeather: () -> Unit,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Dock items (Bottom 5 primary apps)
    val dockAppIds = listOf(AppId.PHONE, AppId.MESSAGES, AppId.SAMSUNG_NOTES, AppId.CAMERA, AppId.BROWSER)
    val dockApps = apps.filter { dockAppIds.contains(it.id) }
    // Grid apps (Excluding dock apps from top grid or showing main launcher apps)
    val gridApps = apps.filter { !dockAppIds.contains(it.id) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -40) {
                        onOpenAppDrawer()
                    }
                }
            }
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(10.dp))

            // Weather & Clock One UI Widget
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .clickable { onOpenWeather() }
                    .testTag("home_weather_widget"),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0x33000000)
                ),
                shape = RoundedCornerShape(22.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = timeString,
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Light
                        )
                        Text(
                            text = dateString,
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${weather.temp}°",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${weather.city} • ${weather.condition}",
                                color = AuraCyan,
                                fontSize = 11.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Weather",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Google / Samsung Search Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onOpenSearch() }
                    .testTag("home_search_bar"),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0x4D000000)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "G",
                            color = Color(0xFF4285F4),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "بحث في الهاتف والويب...",
                            color = Color(0xFFB0B7C3),
                            fontSize = 13.sp
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Lens",
                            tint = AuraCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4-Column Apps Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(gridApps, key = { it.id.name }) { app ->
                    AppIconSquircle(
                        app = app,
                        onClick = { onOpenApp(app.id) }
                    )
                }
            }
        }

        // Bottom Section: Page Indicator & Dock Apps
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0x66FFFFFF))
                )
            }

            // Bottom Dock Container
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0x33000000))
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                dockApps.forEach { app ->
                    AppIconSquircle(
                        app = app,
                        showLabel = false,
                        iconSize = 50,
                        onClick = { onOpenApp(app.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AppIconSquircle(
    app: AppItem,
    onClick: () -> Unit,
    showLabel: Boolean = true,
    iconSize: Int = 48
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(2.dp)
            .testTag("app_icon_${app.id.name.lowercase()}")
    ) {
        Box(
            modifier = Modifier
                .size(iconSize.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(app.iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getIconForApp(app.id),
                contentDescription = app.name,
                tint = Color.White,
                modifier = Modifier.size((iconSize * 0.52).dp)
            )

            if (app.notificationCount > 0) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Color(0xFFFF3B30)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${app.notificationCount}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = app.name,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

fun getIconForApp(appId: AppId): ImageVector {
    return when (appId) {
        AppId.PHONE -> Icons.Default.Call
        AppId.MESSAGES -> Icons.Default.ChatBubble
        AppId.SAMSUNG_NOTES -> Icons.Default.EditNote
        AppId.CAMERA -> Icons.Default.PhotoCamera
        AppId.GALLERY -> Icons.Default.PhotoLibrary
        AppId.VOICE_RECORDER -> Icons.Default.Mic
        AppId.FILE_MANAGER -> Icons.Default.FolderOpen
        AppId.SNAKE_GAME -> Icons.Default.SportsEsports
        AppId.CALCULATOR -> Icons.Default.Calculate
        AppId.CLOCK -> Icons.Default.AccessTime
        AppId.SETTINGS -> Icons.Default.Settings
        AppId.BROWSER -> Icons.Default.Language
        AppId.MUSIC -> Icons.Default.MusicNote
        AppId.WEATHER -> Icons.Default.Cloud
    }
}
