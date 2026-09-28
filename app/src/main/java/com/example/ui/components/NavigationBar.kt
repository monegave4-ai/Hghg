package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun NavigationBar(
    onRecentsClick: () -> Unit,
    onHomeClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true
) {
    val barColor = if (isDarkTheme) Color(0xFF000000).copy(alpha = 0.85f) else Color(0xFFFFFFFF).copy(alpha = 0.85f)
    val iconColor = if (isDarkTheme) Color(0xFFE2E8F0) else Color(0xFF2D3748)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(barColor)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Recents Button (III)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .testTag("nav_recents_button")
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = 24.dp)
                ) { onRecentsClick() },
            contentAlignment = Alignment.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .height(13.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(iconColor)
                    )
                }
            }
        }

        // Home Button (Square / Rounded Home)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .testTag("nav_home_button")
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = 24.dp)
                ) { onHomeClick() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.Transparent)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Circle,
                    contentDescription = "Home",
                    tint = iconColor,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        // Back Button (<)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .testTag("nav_back_button")
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = 24.dp)
                ) { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
