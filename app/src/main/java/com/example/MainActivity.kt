package com.example

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppId
import com.example.ui.apps.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.PhoneViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: PhoneViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemBars()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        setContent {
            val settings by viewModel.settingsState.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = settings.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    PhoneRootContainer(viewModel = viewModel)
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    private fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
}

@Composable
fun PhoneRootContainer(viewModel: PhoneViewModel) {
    val isLocked by viewModel.isLocked.collectAsStateWithLifecycle()
    val currentApp by viewModel.currentApp.collectAsStateWithLifecycle()
    val isAppDrawerOpen by viewModel.isAppDrawerOpen.collectAsStateWithLifecycle()
    val isQuickSettingsOpen by viewModel.isQuickSettingsOpen.collectAsStateWithLifecycle()
    val isEdgePanelOpen by viewModel.isEdgePanelOpen.collectAsStateWithLifecycle()
    val isSPenMenuOpen by viewModel.isSPenMenuOpen.collectAsStateWithLifecycle()
    val isRecentsOpen by viewModel.isRecentsViewOpen.collectAsStateWithLifecycle()
    val recentApps by viewModel.recentApps.collectAsStateWithLifecycle()
    val isFloatingVideoOpen by viewModel.isFloatingVideoOpen.collectAsStateWithLifecycle()
    val isSupabaseDialogOpen by viewModel.isSupabaseConfigDialogOpen.collectAsStateWithLifecycle()

    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val timeString by viewModel.currentTimeString.collectAsStateWithLifecycle()
    val dateString by viewModel.currentDateString.collectAsStateWithLifecycle()
    val batteryLevel by viewModel.batteryLevel.collectAsStateWithLifecycle()
    val isCharging by viewModel.isCharging.collectAsStateWithLifecycle()
    val isWifi by viewModel.isWifiEnabled.collectAsStateWithLifecycle()
    val isBluetooth by viewModel.isBluetoothEnabled.collectAsStateWithLifecycle()
    val isFlashlight by viewModel.isFlashlightOn.collectAsStateWithLifecycle()
    val isMuted by viewModel.isSoundMuted.collectAsStateWithLifecycle()
    val networkType by viewModel.networkTypeLabel.collectAsStateWithLifecycle()

    val callState by viewModel.callState.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val contacts by viewModel.contactsList.collectAsStateWithLifecycle()
    val messages by viewModel.allMessages.collectAsStateWithLifecycle()
    val notes by viewModel.notesList.collectAsStateWithLifecycle()
    val recordings by viewModel.recordingsList.collectAsStateWithLifecycle()
    val virtualFiles by viewModel.virtualFiles.collectAsStateWithLifecycle()
    val weatherData by viewModel.weatherData.collectAsStateWithLifecycle()

    // Recording state
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val isRecordingPaused by viewModel.isRecordingPaused.collectAsStateWithLifecycle()
    val recordingDuration by viewModel.recordingDurationSeconds.collectAsStateWithLifecycle()
    val amplitudes by viewModel.recordingAmplitudes.collectAsStateWithLifecycle()

    // Snake game state
    val snakeState by viewModel.snakeGame.collectAsStateWithLifecycle()

    // Stopwatch & Timer state
    val stopwatchMs by viewModel.stopwatchTimeMs.collectAsStateWithLifecycle()
    val isStopwatchRunning by viewModel.isStopwatchRunning.collectAsStateWithLifecycle()
    val stopwatchLaps by viewModel.stopwatchLaps.collectAsStateWithLifecycle()
    val timerRemaining by viewModel.timerSecondsRemaining.collectAsStateWithLifecycle()
    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()

    // Music state
    val currentTrackIndex by viewModel.currentTrackIndex.collectAsStateWithLifecycle()
    val isMusicPlaying by viewModel.isMusicPlaying.collectAsStateWithLifecycle()
    val musicPositionSeconds by viewModel.musicPositionSeconds.collectAsStateWithLifecycle()

    // Handle Back Press inside virtual phone OS
    BackHandler(enabled = isLocked || currentApp != null || isAppDrawerOpen || isQuickSettingsOpen || isEdgePanelOpen || isSPenMenuOpen || isRecentsOpen) {
        viewModel.goBack()
    }

    PhoneHardwareFrame {
        // Wallpaper Layer
        Box(modifier = Modifier.fillMaxSize()) {
            if (settings.wallpaperId == "aura_glow") {
                Image(
                    painter = painterResource(id = R.drawable.aura_glow_wp_1790583782504),
                    contentDescription = "Wallpaper",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0F141F), Color(0xFF192338), Color(0xFF0D1017))
                            )
                        )
                )
            }

            // Dim overlay if brightness lowered
            if (settings.screenBrightness < 1.0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 1.0f - settings.screenBrightness))
                )
            }

            // Main OS Display Column (Edge-to-Edge Full Screen)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 2.dp)
            ) {
                // One UI Status Bar with Punch Hole Camera and Real Live Telemetry
                StatusBar(
                    timeString = timeString,
                    batteryLevel = batteryLevel,
                    isCharging = isCharging,
                    isWifi = isWifi,
                    networkType = networkType,
                    isMuted = isMuted,
                    hasNotifications = notifications.isNotEmpty(),
                    onStatusClick = { viewModel.toggleQuickSettings() },
                    isLightContent = true
                )

                // Middle Phone Work Area (Apps, Home, Lockscreen)
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (isLocked) {
                        LockScreen(
                            viewModel = viewModel,
                            timeString = timeString,
                            dateString = dateString,
                            batteryLevel = batteryLevel,
                            onQuickApp = { appId ->
                                viewModel.toggleLockScreen(false)
                                viewModel.openApp(appId)
                            }
                        )
                    } else if (currentApp != null) {
                        // Render Active Virtual App
                        when (currentApp) {
                            AppId.PHONE -> PhoneDialerApp(
                                contacts = contacts,
                                onStartCall = { name, phone -> viewModel.startCall(name, phone) },
                                onSendMessage = { phone, name ->
                                    viewModel.openApp(AppId.MESSAGES)
                                },
                                onAddContact = { name, phone, email -> viewModel.addContact(name, phone, email) },
                                onToggleFavorite = { contact -> viewModel.toggleContactFavorite(contact) },
                                onDeleteContact = { contact -> viewModel.deleteContact(contact) },
                                onBack = { viewModel.goHome() }
                            )

                            AppId.MESSAGES -> MessagesApp(
                                messages = messages,
                                contacts = contacts,
                                onSendMessage = { phone, name, text -> viewModel.sendMessage(phone, name, text) },
                                onBack = { viewModel.goHome() }
                            )

                            AppId.SAMSUNG_NOTES -> SamsungNotesApp(
                                notes = notes,
                                onSaveNote = { title, content, strokes, color ->
                                    viewModel.saveNote(title, content, strokes, color)
                                },
                                onDeleteNote = { note -> viewModel.deleteNote(note) },
                                onBack = { viewModel.goHome() }
                            )

                            AppId.CAMERA -> CameraApp(
                                onCapturePhoto = { photoUri -> viewModel.addCapturedPhoto(photoUri) },
                                onOpenGallery = { viewModel.openApp(AppId.GALLERY) },
                                onBack = { viewModel.goHome() }
                            )

                            AppId.VOICE_RECORDER -> VoiceRecorderApp(
                                isRecording = isRecording,
                                isPaused = isRecordingPaused,
                                durationSeconds = recordingDuration,
                                amplitudes = amplitudes,
                                recordings = recordings,
                                onStartRecording = { viewModel.startVoiceRecording() },
                                onPauseRecording = { viewModel.pauseVoiceRecording() },
                                onStopAndSaveRecording = { title -> viewModel.stopAndSaveVoiceRecording(title) },
                                onDeleteRecording = { rec -> viewModel.deleteRecording(rec) },
                                onBack = { viewModel.goHome() }
                            )

                            AppId.GALLERY -> GalleryApp(
                                viewModel = viewModel,
                                onBack = { viewModel.goHome() }
                            )

                            AppId.FILE_MANAGER -> FileManagerApp(
                                files = virtualFiles,
                                onBack = { viewModel.goHome() }
                            )

                            AppId.GALAXY_STORE -> GalaxyStoreApp(
                                viewModel = viewModel
                            )

                            AppId.WHATSAPP -> WhatsAppApp(
                                viewModel = viewModel
                            )

                            AppId.TIC_TAC_TOE -> TicTacToeOnlineApp(
                                viewModel = viewModel
                            )

                            AppId.PHOTO_EDITOR -> PhotoEditorApp(
                                viewModel = viewModel
                            )

                            AppId.BRICK_BREAKER -> BrickBreakerApp(
                                viewModel = viewModel
                            )

                            AppId.DEVICE_CARE -> DeviceCareApp(
                                viewModel = viewModel
                            )

                            AppId.SNAKE_GAME -> SnakeGameApp(
                                gameState = snakeState,
                                onChangeDirection = { dir -> viewModel.changeSnakeDirection(dir) },
                                onRestartGame = { viewModel.startSnakeGame() },
                                onTogglePause = { viewModel.toggleSnakePause() },
                                onBack = { viewModel.goHome() }
                            )

                            AppId.CALCULATOR -> CalculatorApp(
                                onBack = { viewModel.goHome() }
                            )

                            AppId.CLOCK -> ClockApp(
                                stopwatchMs = stopwatchMs,
                                isStopwatchRunning = isStopwatchRunning,
                                laps = stopwatchLaps,
                                onStartStopwatch = { viewModel.startStopwatch() },
                                onPauseStopwatch = { viewModel.pauseStopwatch() },
                                onResetStopwatch = { viewModel.resetStopwatch() },
                                onAddLap = { viewModel.addLap() },
                                timerRemaining = timerRemaining,
                                isTimerRunning = isTimerRunning,
                                onStartTimer = { sec -> viewModel.startTimer(sec) },
                                onStopTimer = { viewModel.stopTimer() },
                                onBack = { viewModel.goHome() }
                            )

                            AppId.SETTINGS -> SettingsApp(
                                viewModel = viewModel,
                                settings = settings,
                                onToggleDarkMode = { viewModel.toggleDarkMode() },
                                onSelectWallpaper = { wp -> viewModel.updateWallpaper(wp) },
                                onBrightnessChange = { b -> viewModel.updateBrightness(b) },
                                onVolumeChange = { v -> viewModel.updateVolume(v) },
                                onBack = { viewModel.goHome() }
                            )

                            AppId.BROWSER -> BrowserApp(
                                onBack = { viewModel.goHome() }
                            )

                            AppId.MUSIC -> MusicApp(
                                playlist = viewModel.musicPlaylist,
                                currentTrackIndex = currentTrackIndex,
                                isPlaying = isMusicPlaying,
                                positionSeconds = musicPositionSeconds,
                                onTogglePlay = { viewModel.togglePlayMusic() },
                                onNext = { viewModel.nextTrack() },
                                onPrev = { viewModel.previousTrack() },
                                onSelectTrack = { idx ->
                                    viewModel.currentTrackIndex.value = idx
                                    viewModel.musicPositionSeconds.value = 0
                                    if (!isMusicPlaying) viewModel.togglePlayMusic()
                                },
                                onBack = { viewModel.goHome() }
                            )

                            AppId.WEATHER -> WeatherApp(
                                weather = weatherData,
                                onBack = { viewModel.goHome() }
                            )

                            else -> HomeScreen(
                                apps = viewModel.baseAppsList,
                                weather = weatherData,
                                timeString = timeString,
                                dateString = dateString,
                                onOpenApp = { appId -> viewModel.openApp(appId) },
                                onOpenAppDrawer = { viewModel.isAppDrawerOpen.value = true },
                                onOpenWeather = { viewModel.openApp(AppId.WEATHER) },
                                onOpenSearch = { viewModel.openApp(AppId.BROWSER) }
                            )
                        }
                    } else {
                        // Home Screen Launcher
                        HomeScreen(
                            apps = viewModel.baseAppsList,
                            weather = weatherData,
                            timeString = timeString,
                            dateString = dateString,
                            onOpenApp = { appId -> viewModel.openApp(appId) },
                            onOpenAppDrawer = { viewModel.isAppDrawerOpen.value = true },
                            onOpenWeather = { viewModel.openApp(AppId.WEATHER) },
                            onOpenSearch = { viewModel.openApp(AppId.BROWSER) }
                        )
                    }

                    // Floating S-Pen Button positioned at the BOTTOM to avoid blocking screen view
                    if (!isLocked && currentApp != AppId.CAMERA) {
                        SPenFloatingButton(
                            onClick = { viewModel.toggleSPenMenu() },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 75.dp, end = 12.dp)
                        )
                    }

                    // Edge Panel Trigger Bar
                    if (!isLocked && settings.edgePanelEnabled) {
                        EdgePanelTrigger(
                            onClick = { viewModel.toggleEdgePanel() },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(top = 100.dp)
                        )
                    }

                    // Floating Picture-in-Picture Video Player Overlay
                    FloatingVideoPlayer(
                        isOpen = isFloatingVideoOpen,
                        onClose = { viewModel.isFloatingVideoOpen.value = false },
                        onExpand = {
                            viewModel.isFloatingVideoOpen.value = false
                            viewModel.openApp(AppId.BROWSER)
                        }
                    )

                    // App Drawer
                    AppDrawer(
                        isOpen = isAppDrawerOpen,
                        apps = viewModel.baseAppsList,
                        onOpenApp = { appId -> viewModel.openApp(appId) },
                        onClose = { viewModel.isAppDrawerOpen.value = false }
                    )

                    // Recent Tasks Switcher
                    RecentsView(
                        isOpen = isRecentsOpen,
                        recentAppIds = recentApps,
                        allApps = viewModel.baseAppsList,
                        onOpenApp = { appId ->
                            viewModel.isRecentsViewOpen.value = false
                            viewModel.openApp(appId)
                        },
                        onCloseAll = {
                            viewModel.recentApps.value = emptyList()
                            viewModel.goHome()
                        },
                        onClose = { viewModel.isRecentsViewOpen.value = false }
                    )

                    // S-Pen Air Command Radial Fan Menu
                    SPenAirCommandMenu(
                        isOpen = isSPenMenuOpen,
                        onOpenNote = {
                            viewModel.isSPenMenuOpen.value = false
                            viewModel.openApp(AppId.SAMSUNG_NOTES)
                        },
                        onOpenScreenWrite = {
                            viewModel.isSPenMenuOpen.value = false
                            viewModel.openApp(AppId.PHOTO_EDITOR)
                        },
                        onOpenLiveMessage = {
                            viewModel.isSPenMenuOpen.value = false
                            viewModel.openApp(AppId.WHATSAPP)
                        },
                        onOpenTranslate = {
                            viewModel.isSPenMenuOpen.value = false
                            viewModel.toggleFloatingVideo()
                        },
                        onClose = { viewModel.isSPenMenuOpen.value = false }
                    )

                    // Curved Edge Panel Overlay
                    EdgePanel(
                        isOpen = isEdgePanelOpen,
                        onOpenApp = { appId ->
                            viewModel.isEdgePanelOpen.value = false
                            viewModel.openApp(appId)
                        },
                        onClose = { viewModel.isEdgePanelOpen.value = false }
                    )

                    // Quick Settings & Notification Drawer
                    QuickSettingsShade(
                        isOpen = isQuickSettingsOpen,
                        timeString = timeString,
                        dateString = dateString,
                        isWifi = isWifi,
                        isBluetooth = isBluetooth,
                        isFlashlight = isFlashlight,
                        isMuted = isMuted,
                        brightness = settings.screenBrightness,
                        notifications = notifications,
                        onToggleWifi = { viewModel.toggleWifi() },
                        onToggleBluetooth = { viewModel.toggleBluetooth() },
                        onToggleFlashlight = { viewModel.toggleFlashlight() },
                        onToggleSound = { viewModel.toggleSoundMute() },
                        onBrightnessChange = { b -> viewModel.updateBrightness(b) },
                        onOpenSettings = {
                            viewModel.isQuickSettingsOpen.value = false
                            viewModel.openApp(AppId.SETTINGS)
                        },
                        onOpenSPen = {
                            viewModel.isQuickSettingsOpen.value = false
                            viewModel.isSPenMenuOpen.value = true
                        },
                        onNotificationClick = { targetApp ->
                            viewModel.isQuickSettingsOpen.value = false
                            if (targetApp != null) viewModel.openApp(targetApp)
                        },
                        onClearNotifications = { viewModel.clearNotifications() },
                        onClose = { viewModel.isQuickSettingsOpen.value = false }
                    )

                    // Active & Incoming Call Overlay
                    InCallOverlay(
                        callState = callState,
                        onAnswer = { viewModel.answerCall() },
                        onEndCall = { viewModel.endCall() },
                        onToggleMute = { viewModel.toggleMute() },
                        onToggleSpeaker = { viewModel.toggleSpeaker() }
                    )

                    // Supabase Cloud Configuration Dialog
                    if (isSupabaseDialogOpen) {
                        SupabaseConfigDialog(
                            viewModel = viewModel,
                            onDismiss = { viewModel.closeSupabaseConfigDialog() }
                        )
                    }
                }

                // One UI Navigation Bar (Recents |||, Home O, Back <)
                NavigationBar(
                    onRecentsClick = { viewModel.toggleRecents() },
                    onHomeClick = { viewModel.goHome() },
                    onBackClick = { viewModel.goBack() },
                    isDarkTheme = true
                )
            }
        }
    }
}
