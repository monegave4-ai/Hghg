package com.example.ui.apps

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.NoteEntity
import com.example.data.model.DrawingStroke
import com.example.data.model.StrokePoint
import com.example.ui.theme.*

@Composable
fun SamsungNotesApp(
    notes: List<NoteEntity>,
    onSaveNote: (String, String, String, String) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var isDrawingMode by remember { mutableStateOf(true) }

    // Drawing Canvas State
    val strokes = remember { mutableStateListOf<DrawingStroke>() }
    var currentStrokePoints by remember { mutableStateOf<List<StrokePoint>>(emptyList()) }
    var selectedColor by remember { mutableStateOf(Color(0xFF0072DE)) }
    var strokeWidth by remember { mutableFloatStateOf(6f) }
    var isEraser by remember { mutableStateOf(false) }

    val paletteColors = listOf(
        Color(0xFF000000),
        Color(0xFF0072DE),
        Color(0xFFE91E63),
        Color(0xFF4CAF50),
        Color(0xFFFF9800),
        Color(0xFF9C27B0),
        Color(0xFF00E5FF),
        Color(0xFFFFD54F)
    )

    if (isEditing) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF131720))
        ) {
            // Editor Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { isEditing = false }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(
                        text = if (isDrawingMode) "رسم S-Pen ✍️" else "ملاحظة نصية 📝",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Switch mode
                    IconButton(onClick = { isDrawingMode = !isDrawingMode }) {
                        Icon(
                            imageVector = if (isDrawingMode) Icons.Default.TextFields else Icons.Default.Brush,
                            contentDescription = "Toggle Mode",
                            tint = SPenGold
                        )
                    }
                    // Undo
                    if (isDrawingMode && strokes.isNotEmpty()) {
                        IconButton(onClick = { if (strokes.isNotEmpty()) strokes.removeLast() }) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = Color.White)
                        }
                    }
                    // Save Button
                    Button(
                        onClick = {
                            onSaveNote(
                                noteTitle.ifBlank { if (isDrawingMode) "لوحة رسم S-Pen" else "ملاحظة جديدة" },
                                noteContent,
                                strokes.size.toString(), // serial summary
                                "#1E2330"
                            )
                            isEditing = false
                            strokes.clear()
                            noteTitle = ""
                            noteContent = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SamsungBlueLight),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("save_note_btn")
                    ) {
                        Text("حفظ", fontSize = 13.sp)
                    }
                }
            }

            // Note Title Input
            OutlinedTextField(
                value = noteTitle,
                onValueChange = { noteTitle = it },
                placeholder = { Text("عنوان الملاحظة...", color = Color(0xFF7E8A9E), fontSize = 14.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E2330),
                    unfocusedContainerColor = Color(0xFF191D28),
                    focusedBorderColor = SPenGold,
                    unfocusedBorderColor = Color(0xFF2B3244),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            if (isDrawingMode) {
                // Drawing Canvas Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF222736))
                        .pointerInput(isEraser, selectedColor, strokeWidth) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentStrokePoints = listOf(StrokePoint(offset.x, offset.y))
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    currentStrokePoints = currentStrokePoints + StrokePoint(change.position.x, change.position.y)
                                },
                                onDragEnd = {
                                    if (currentStrokePoints.isNotEmpty()) {
                                        strokes.add(
                                            DrawingStroke(
                                                points = currentStrokePoints,
                                                color = if (isEraser) 0xFF222736 else selectedColor.value.toLong(),
                                                width = if (isEraser) strokeWidth * 3f else strokeWidth,
                                                isEraser = isEraser
                                            )
                                        )
                                        currentStrokePoints = emptyList()
                                    }
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Draw finalized strokes
                        strokes.forEach { stroke ->
                            if (stroke.points.size > 1) {
                                val path = Path()
                                path.moveTo(stroke.points.first().x, stroke.points.first().y)
                                for (i in 1 until stroke.points.size) {
                                    path.lineTo(stroke.points[i].x, stroke.points[i].y)
                                }
                                drawPath(
                                    path = path,
                                    color = Color(stroke.color.toULong()),
                                    style = Stroke(
                                        width = stroke.width,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }

                        // Draw live active stroke
                        if (currentStrokePoints.size > 1) {
                            val livePath = Path()
                            livePath.moveTo(currentStrokePoints.first().x, currentStrokePoints.first().y)
                            for (i in 1 until currentStrokePoints.size) {
                                livePath.lineTo(currentStrokePoints[i].x, currentStrokePoints[i].y)
                            }
                            drawPath(
                                path = livePath,
                                color = if (isEraser) Color(0xFF222736) else selectedColor,
                                style = Stroke(
                                    width = if (isEraser) strokeWidth * 3f else strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }

                // S-Pen Styling Controls (Palette, Pen Width, Eraser)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF191D28))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Eraser Toggle
                    IconButton(
                        onClick = { isEraser = !isEraser },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isEraser) SPenGold else Color(0xFF2A3142))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = "Eraser",
                            tint = if (isEraser) Color.Black else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Colors Palette
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                    ) {
                        items(paletteColors) { color ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable {
                                        selectedColor = color
                                        isEraser = false
                                    }
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColor == color && !isEraser) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }
                        }
                    }

                    // Stroke width picker
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { strokeWidth = maxOf(2f, strokeWidth - 2f) }) {
                            Text("-", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("${strokeWidth.toInt()}", color = Color.White, fontSize = 12.sp)
                        IconButton(onClick = { strokeWidth = minOf(24f, strokeWidth + 2f) }) {
                            Text("+", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Text Note Mode
                OutlinedTextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    placeholder = { Text("اكتب محتوى الملاحظة هنا...", color = Color(0xFF7E8A9E), fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E2330),
                        unfocusedContainerColor = Color(0xFF191D28),
                        focusedBorderColor = SPenGold,
                        unfocusedBorderColor = Color(0xFF2B3244),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        }
    } else {
        // Notes List View
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
                        text = "Samsung Notes (S-Pen)",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Add Note / S-Pen Action
                IconButton(
                    onClick = {
                        isEditing = true
                        isDrawingMode = true
                        strokes.clear()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(NotesYellow)
                        .testTag("add_note_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "New Note",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = NotesYellow,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "لا توجد ملاحظات محفوظة",
                            color = Color(0xFF8E99A8),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "استخدم قلم S-Pen لإنشاء رسومات وملاحظات مذهلة",
                            color = Color(0xFF5A6375),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B202C))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(NotesYellow)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = note.title,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteNote(note) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFF8E99A8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                if (note.content.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = note.content,
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 13.sp,
                                        maxLines = 3
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
