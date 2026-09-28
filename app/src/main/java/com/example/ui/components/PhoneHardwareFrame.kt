package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PhoneHardwareFrame(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    // Note 10 Immersive Full-Screen Frame with Side Edge Highlights
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Main Screen Viewport
        Box(
            modifier = Modifier.fillMaxSize(),
            content = content
        )

        // Subtle Note 10 Curved Infinity Glass Side Edge Reflections
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.5.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0x33FFFFFF), Color.Transparent)
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.5.dp)
                .align(androidx.compose.ui.Alignment.CenterEnd)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, Color(0x33FFFFFF))
                    )
                )
        )
    }
}
