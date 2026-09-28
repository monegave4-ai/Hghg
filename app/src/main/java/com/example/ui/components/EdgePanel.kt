package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppId
import com.example.ui.theme.*

@Composable
fun EdgePanelTrigger(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(5.dp)
            .height(72.dp)
            .clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
            .background(AuraCyan.copy(alpha = 0.7f))
            .clickable { onClick() }
    )
}

@Composable
fun EdgePanel(
    isOpen: Boolean,
    onOpenApp: (AppId) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x99000000))
                .clickable { onClose() },
            contentAlignment = Alignment.CenterEnd
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.75f)
                    .width(130.dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFA1E2330), Color(0xFA141722))
                        )
                    )
                    .clickable(enabled = false) {}
                    .padding(vertical = 20.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "Apps Edge",
                    color = AuraCyan,
                    fontSize = 12.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                EdgeAppItem(
                    title = "ملاحظات",
                    icon = Icons.Default.Edit,
                    color = NotesYellow,
                    onClick = { onOpenApp(AppId.SAMSUNG_NOTES) }
                )
                EdgeAppItem(
                    title = "الكاميرا",
                    icon = Icons.Default.PhotoCamera,
                    color = CameraRed,
                    onClick = { onOpenApp(AppId.CAMERA) }
                )
                EdgeAppItem(
                    title = "الحاسبة",
                    icon = Icons.Default.Calculate,
                    color = CalculatorTeal,
                    onClick = { onOpenApp(AppId.CALCULATOR) }
                )
                EdgeAppItem(
                    title = "لعبة الدودة",
                    icon = Icons.Default.SportsEsports,
                    color = SnakeGreen,
                    onClick = { onOpenApp(AppId.SNAKE_GAME) }
                )
                EdgeAppItem(
                    title = "ملفاتي",
                    icon = Icons.Default.Folder,
                    color = FilesOrange,
                    onClick = { onOpenApp(AppId.FILE_MANAGER) }
                )
            }
        }
    }
}

@Composable
fun EdgeAppItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 10.sp
        )
    }
}
