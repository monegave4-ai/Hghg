package com.example.ui.apps

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.AuraCyan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CameraApp(
    onCapturePhoto: (String) -> Unit,
    onOpenGallery: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var selectedZoom by remember { mutableStateOf("1.0x") }
    var selectedMode by remember { mutableStateOf("صورة") } // صورة, بورتريه, احترافي, فيديو
    var isFlashOn by remember { mutableStateOf(false) }
    var showFlashEffect by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val modes = listOf("بورتريه", "صورة", "فيديو", "احترافي")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera Viewfinder (Live CameraX if permission granted, otherwise stylish simulator viewfinder)
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(lensFacing)
                                .build()
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                        } catch (_: Exception) {}
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Simulated Viewfinder
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0B1120))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "منظر الكاميرا المباشر (Galaxy Note 10)",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "اضغط على زر الالتقاط لأخذ صورة وحفظها في المعرض",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Camera Grid & Focus reticle
        Box(
            modifier = Modifier
                .size(70.dp)
                .align(Alignment.Center)
                .border(1.dp, AuraCyan.copy(alpha = 0.6f), CircleShape)
        )

        // Top Toolbar (Flash, Aspect, Timer, Settings, Back)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(Color(0x66000000))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(onClick = { isFlashOn = !isFlashOn }) {
                    Icon(
                        imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flash",
                        tint = if (isFlashOn) Color(0xFFFFD54F) else Color.White
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Timer",
                        tint = Color.White
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.FilterVintage,
                        contentDescription = "Filters",
                        tint = Color.White
                    )
                }
            }
        }

        // Galaxy Note 10 Zoom Lens Selector (0.5x Ultra Wide, 1.0x Wide, 2.0x Telephoto)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 150.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x80000000))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf("0.5x", "1.0x", "2.0x").forEach { zoom ->
                Text(
                    text = zoom,
                    color = if (selectedZoom == zoom) Color(0xFFFFD54F) else Color.White,
                    fontSize = 12.sp,
                    fontWeight = if (selectedZoom == zoom) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedZoom = zoom }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Camera Modes Selector (Photo, Pro, Live Focus, Video)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            modes.forEach { mode ->
                Text(
                    text = mode,
                    color = if (selectedMode == mode) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    fontWeight = if (selectedMode == mode) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .clickable { selectedMode = mode }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        // Bottom Controls Row: Gallery Shortcut, Big Shutter Button, Switch Camera Lens
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color(0x99000000))
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gallery Thumbnail Button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF262C3D))
                    .clickable { onOpenGallery() }
                    .testTag("camera_gallery_thumbnail_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Gallery",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Big White Shutter Button
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .border(4.dp, Color.White, CircleShape)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable {
                        // Flash animation
                        coroutineScope.launch {
                            showFlashEffect = true
                            delay(120)
                            showFlashEffect = false
                        }
                        val photoId = "photo_${System.currentTimeMillis()}"
                        onCapturePhoto(photoId)
                    }
                    .testTag("camera_shutter_btn")
            )

            // Switch Camera Lens (Front/Back)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0x4DFFFFFF))
                    .clickable {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    }
                    .testTag("camera_switch_lens_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FlipCameraAndroid,
                    contentDescription = "Switch Camera",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // Shutter White Flash Screen Overlay
        if (showFlashEffect) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            )
        }
    }
}
