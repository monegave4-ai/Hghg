package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@Composable
fun PhoneHardwareFrame(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    // Note 10 Outer Frame styling
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030508))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Metallic edge and glass frame
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(16.dp, RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E222D), Color(0xFF0D0F14), Color(0xFF181C26))
                    )
                )
                .border(
                    width = 2.5.dp,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF4B5563), Color(0xFF1F2937), Color(0xFF6B7280))
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(3.dp)
        ) {
            // Screen display area with curved edges
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.Black),
                content = content
            )
        }
    }
}
