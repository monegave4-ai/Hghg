package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppId
import com.example.ui.theme.*

@Composable
fun SPenFloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(46.dp)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF2C3140), Color(0xFF1B1E29))
                )
            )
            .testTag("spen_floating_trigger")
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "S-Pen Air Command",
            tint = SPenGold,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun SPenAirCommandMenu(
    isOpen: Boolean,
    onOpenNote: () -> Unit,
    onOpenScreenWrite: () -> Unit,
    onOpenLiveMessage: () -> Unit,
    onOpenTranslate: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn() + scaleIn(initialScale = 0.6f, transformOrigin = androidx.compose.ui.graphics.TransformOrigin(1f, 0.5f)),
        exit = fadeOut() + scaleOut(targetScale = 0.6f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC090B10))
                .clickable { onClose() },
            contentAlignment = Alignment.CenterEnd
        ) {
            Column(
                modifier = Modifier
                    .padding(end = 24.dp)
                    .width(260.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1F2433), Color(0xFF131722))
                        )
                    )
                    .clickable(enabled = false) {}
                    .padding(20.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Air Command (S-Pen)",
                            color = SPenGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = SPenGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Divider(color = Color(0xFF333B4F))

                SPenMenuItem(
                    title = "إنشاء ملاحظة بالرسم",
                    subtitle = "Create Note & Sketch",
                    icon = Icons.Default.Brush,
                    color = NotesYellow,
                    onClick = onOpenNote
                )

                SPenMenuItem(
                    title = "الكتابة على الشاشة",
                    subtitle = "Screen Write",
                    icon = Icons.Default.Screenshot,
                    color = AuraCyan,
                    onClick = onOpenScreenWrite
                )

                SPenMenuItem(
                    title = "الرسائل الحية",
                    subtitle = "Live Messages",
                    icon = Icons.Default.AutoAwesome,
                    color = AuraMagenta,
                    onClick = onOpenLiveMessage
                )

                SPenMenuItem(
                    title = "الترجمة الفورية",
                    subtitle = "Translate S-Pen",
                    icon = Icons.Default.Translate,
                    color = SamsungBlueLight,
                    onClick = onOpenTranslate
                )
            }
        }
    }
}

@Composable
fun SPenMenuItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = Color(0xFF8C96A8),
                fontSize = 10.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
