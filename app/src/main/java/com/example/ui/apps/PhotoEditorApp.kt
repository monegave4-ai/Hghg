package com.example.ui.apps

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DrawingStroke
import com.example.data.model.PhotoFilterType
import com.example.data.model.StrokePoint
import com.example.viewmodel.PhoneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditorApp(viewModel: PhoneViewModel) {
    var selectedFilter by remember { mutableStateOf(PhotoFilterType.NONE) }
    var selectedColor by remember { mutableStateOf(Color(0xFFE91E63)) }
    var strokeWidth by remember { mutableStateOf(6f) }
    var isEraser by remember { mutableStateOf(false) }
    var isSpenMode by remember { mutableStateOf(true) }

    val strokes = remember { mutableStateListOf<DrawingStroke>() }
    var currentPoints by remember { mutableStateOf<List<StrokePoint>>(emptyList()) }

    val filterOptions = listOf(
        PhotoFilterType.NONE to "الأصلي",
        PhotoFilterType.VINTAGE to "عتيق (Vintage)",
        PhotoFilterType.NOIR to "أبيض وأسود",
        PhotoFilterType.CYBERPUNK to "سايبربانك",
        PhotoFilterType.WARM_SUNSET to "غروب دافئ",
        PhotoFilterType.COLD_AURORA to "أورورا نوت",
        PhotoFilterType.NEON_GLOW to "نيون ساطع"
    )

    val colorPalette = listOf(
        Color(0xFFE91E63),
        Color(0xFF0072DE),
        Color(0xFFFFEB3B),
        Color(0xFF4CAF50),
        Color(0xFF9C27B0),
        Color(0xFFFFFFFF),
        Color(0xFF000000)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Brush, contentDescription = null, tint = Color(0xFFFF5722))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("محرر الصور الاحترافي S-Pen", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                actions = {
                    // Undo
                    IconButton(
                        onClick = { if (strokes.isNotEmpty()) strokes.removeLast() },
                        enabled = strokes.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = "تراجع")
                    }

                    // Save to Gallery
                    Button(
                        onClick = {
                            viewModel.addCapturedPhoto(
                                photoUri = "edited_photo_${System.currentTimeMillis()}",
                                filter = selectedFilter.name
                            )
                            viewModel.vibrate(50)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ في الاستوديو", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Filters Row
                Text("فلاتر الصورة والألوان:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filterOptions) { (filter, label) ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFFFF5722) else MaterialTheme.colorScheme.surface,
                            modifier = Modifier.clickable {
                                selectedFilter = filter
                                viewModel.vibrate(15)
                            }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Drawing Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Color Picker
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        colorPalette.forEach { col ->
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .clickable {
                                        selectedColor = col
                                        isEraser = false
                                        viewModel.vibrate(10)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColor == col && !isEraser) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = if (col == Color.White) Color.Black else Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    // Pen / Eraser Toggles
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = { isEraser = false; viewModel.vibrate(10) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isEraser) Color(0xFFFF5722) else Color.Transparent)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "قلم", tint = if (!isEraser) Color.White else MaterialTheme.colorScheme.onSurface)
                        }

                        IconButton(
                            onClick = { isEraser = true; viewModel.vibrate(10) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isEraser) Color(0xFFFF5722) else Color.Transparent)
                        ) {
                            Icon(Icons.Default.AutoFixNormal, contentDescription = "ممحاة", tint = if (isEraser) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF121212)),
            contentAlignment = Alignment.Center
        ) {
            // Photo Canvas Area
            val filterOverlay = when (selectedFilter) {
                PhotoFilterType.VINTAGE -> Brush.radialGradient(listOf(Color(0x55D7CCC8), Color(0x775D4037)))
                PhotoFilterType.NOIR -> Brush.verticalGradient(listOf(Color(0x88212121), Color(0xAA000000)))
                PhotoFilterType.CYBERPUNK -> Brush.linearGradient(listOf(Color(0x5500E5FF), Color(0x66E040FB)))
                PhotoFilterType.WARM_SUNSET -> Brush.verticalGradient(listOf(Color(0x66FF5722), Color(0x66FF9800)))
                PhotoFilterType.COLD_AURORA -> Brush.linearGradient(listOf(Color(0x5500BCD4), Color(0x669C27B0)))
                PhotoFilterType.NEON_GLOW -> Brush.radialGradient(listOf(Color(0x6600E676), Color(0x7700B0FF)))
                PhotoFilterType.NONE -> Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
            }

            Box(
                modifier = Modifier
                    .fillMaxSize(0.92f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1E3C72), Color(0xFF2A5298), Color(0xFFE91E63))
                        )
                    )
            ) {
                // Filter Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(filterOverlay)
                )

                // Simulated Background Scenery
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    Column {
                        Text("Samsung Note 10 Studio", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("ارسم واكتب بقلم S-Pen مباشرة على الصورة", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                    }
                }

                // Interactive Drawing Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(selectedColor, strokeWidth, isEraser) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPoints = listOf(StrokePoint(offset.x, offset.y))
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    currentPoints = currentPoints + StrokePoint(change.position.x, change.position.y)
                                },
                                onDragEnd = {
                                    if (currentPoints.isNotEmpty()) {
                                        strokes.add(
                                            DrawingStroke(
                                                points = currentPoints,
                                                color = if (isEraser) 0xFF121212 else selectedColor.value.toLong(),
                                                width = if (isEraser) strokeWidth * 3 else strokeWidth,
                                                isEraser = isEraser
                                            )
                                        )
                                        currentPoints = emptyList()
                                    }
                                }
                            )
                        }
                ) {
                    // Render past strokes
                    strokes.forEach { stroke ->
                        if (stroke.points.size > 1) {
                            val strokeColor = Color(stroke.color.toULong())
                            for (i in 0 until stroke.points.size - 1) {
                                val p1 = stroke.points[i]
                                val p2 = stroke.points[i + 1]
                                drawLine(
                                    color = strokeColor,
                                    start = Offset(p1.x, p1.y),
                                    end = Offset(p2.x, p2.y),
                                    strokeWidth = stroke.width,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }

                    // Render current active stroke
                    if (currentPoints.size > 1) {
                        val activeColor = if (isEraser) Color(0xFF121212) else selectedColor
                        for (i in 0 until currentPoints.size - 1) {
                            val p1 = currentPoints[i]
                            val p2 = currentPoints[i + 1]
                            drawLine(
                                color = activeColor,
                                start = Offset(p1.x, p1.y),
                                end = Offset(p2.x, p2.y),
                                strokeWidth = if (isEraser) strokeWidth * 3 else strokeWidth,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }
    }
}
