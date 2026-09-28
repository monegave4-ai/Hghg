package com.example.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import com.example.data.model.*
import com.example.data.remote.FirestoreManager
import com.example.data.remote.SupabaseConnectionStatus
import com.example.data.remote.SupabaseManager
import com.example.ui.theme.*
import com.example.util.SoundManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class PhoneViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val context: Context = application.applicationContext
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    val firestoreManager = FirestoreManager()
    val supabaseManager = SupabaseManager(context)
    val soundManager = SoundManager(context)

    // Supabase Connection State
    val supabaseStatus = MutableStateFlow(
        if (supabaseManager.getConfig().isConfigured) SupabaseConnectionStatus.CONNECTED
        else SupabaseConnectionStatus.NOT_CONFIGURED
    )
    val supabaseStatusMessage = MutableStateFlow("")
    val isSupabaseConfigDialogOpen = MutableStateFlow(false)

    // System Navigation State
    val isLocked = MutableStateFlow(false)
    val currentApp = MutableStateFlow<AppId?>(null)
    val isAppDrawerOpen = MutableStateFlow(false)
    val isQuickSettingsOpen = MutableStateFlow(false)
    val isEdgePanelOpen = MutableStateFlow(false)
    val isSPenMenuOpen = MutableStateFlow(false)
    val recentApps = MutableStateFlow<List<AppId>>(emptyList())
    val isRecentsViewOpen = MutableStateFlow(false)

    // Floating Video Player State (Picture-in-Picture)
    val isFloatingVideoOpen = MutableStateFlow(false)

    // Device Settings State
    val settingsState = MutableStateFlow(
        AppSettingsEntity(
            wallpaperId = "aura_glow",
            isDarkMode = true,
            isLockScreenEnabled = false,
            lockPin = "1234",
            isFingerprintEnabled = true,
            isFaceUnlockEnabled = true,
            screenBrightness = 0.95f,
            mediaVolume = 0.85f,
            sPenSoundEnabled = true,
            edgePanelEnabled = true,
            currentWhatsAppUsername = "@galaxy_user"
        )
    )

    // Real Live Device Telemetry State
    val currentTimeString = MutableStateFlow("12:00")
    val currentDateString = MutableStateFlow("الاثنين، ٢٨ سبتمبر")
    val batteryLevel = MutableStateFlow(100)
    val isCharging = MutableStateFlow(false)
    val isWifiEnabled = MutableStateFlow(true)
    val isCellularConnected = MutableStateFlow(true)
    val isBluetoothEnabled = MutableStateFlow(true)
    val isFlashlightOn = MutableStateFlow(false)
    val isSoundMuted = MutableStateFlow(false)
    val networkTypeLabel = MutableStateFlow("5G")

    // Lockscreen Biometrics & Security State
    val pinInput = MutableStateFlow("")
    val isFaceScanning = MutableStateFlow(false)
    val isFingerprintScanning = MutableStateFlow(false)
    val lockScreenError = MutableStateFlow<String?>(null)
    val isFaceUnlocked = MutableStateFlow(false)

    // Call System State
    val callState = MutableStateFlow(CallState())
    private var callJob: Job? = null

    // Notifications State
    val notifications = MutableStateFlow<List<NotificationModel>>(
        listOf(
            NotificationModel(
                id = "n_welcome",
                title = "Samsung Galaxy Note 10",
                message = "تم تفعيل الرسائل الفورية الحقيقية ومتجر التطبيقات مع دعم Supabase Cloud.",
                appName = "النظام",
                timestampFormatted = "الآن",
                targetApp = AppId.WHATSAPP
            )
        )
    )

    // Database Flows
    val contactsList = db.contactDao().getAllContacts().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allMessages = db.messageDao().getAllMessages().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val notesList = db.noteDao().getAllNotes().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val recordingsList = db.recordingDao().getAllRecordings().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val storeApps = db.storeAppDao().getAllStoreApps().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val installedStoreApps = db.storeAppDao().getInstalledApps().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val activeGalleryPhotos = db.galleryPhotoDao().getActivePhotos().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val trashGalleryPhotos = db.galleryPhotoDao().getTrashPhotos().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val whatsAppUsers = db.whatsAppDao().getAllUsers().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val selectedWhatsAppUser = MutableStateFlow<WhatsAppUserEntity?>(null)

    // Base Apps Catalog
    val baseAppsList: List<AppItem> = listOf(
        AppItem(AppId.PHONE, "الهاتف", "phone", PhoneGreen),
        AppItem(AppId.MESSAGES, "الرسائل", "message", MessagesBlue, notificationCount = 1),
        AppItem(AppId.SAMSUNG_NOTES, "الملاحظات", "edit_note", NotesYellow),
        AppItem(AppId.CAMERA, "الكاميرا", "camera", CameraRed),
        AppItem(AppId.WHATSAPP, "واتساب", "chat", Color(0xFF25D366)),
        AppItem(AppId.GALAXY_STORE, "Galaxy Store", "shopping_bag", Color(0xFFE91E63)),
        AppItem(AppId.PHOTO_EDITOR, "تعديل الصور", "brush", Color(0xFF9C27B0)),
        AppItem(AppId.BRICK_BREAKER, "كسار الطوب", "gamepad", Color(0xFFFF5722)),
        AppItem(AppId.DEVICE_CARE, "العناية بالجهاز", "speed", Color(0xFF00BCD4)),
        AppItem(AppId.TIC_TAC_TOE, "XO أونلاين", "grid_view", Color(0xFF673AB7)),
        AppItem(AppId.GALLERY, "الاستوديو", "photo", GalleryPurple),
        AppItem(AppId.FILE_MANAGER, "ملفاتي", "folder", FilesOrange),
        AppItem(AppId.VOICE_RECORDER, "المسجل", "mic", RecorderPink),
        AppItem(AppId.SNAKE_GAME, "لعبة الثعبان", "sports_esports", SnakeGreen),
        AppItem(AppId.BROWSER, "الإنترنت", "public", SamsungBlueLight),
        AppItem(AppId.MUSIC, "الموسيقى", "music_note", MusicPink),
        AppItem(AppId.CALCULATOR, "الحاسبة", "calculate", CalculatorTeal),
        AppItem(AppId.CLOCK, "الساعة", "alarm", ClockNavy),
        AppItem(AppId.WEATHER, "الطقس", "wb_sunny", WeatherSky),
        AppItem(AppId.SETTINGS, "الضبط", "settings", SettingsSlate)
    )

    // Voice Recorder State
    val isRecording = MutableStateFlow(false)
    val isRecordingPaused = MutableStateFlow(false)
    val recordingDurationSeconds = MutableStateFlow(0)
    val recordingAmplitudes = MutableStateFlow<List<Float>>(emptyList())
    private var recordingJob: Job? = null

    // Snake Game State
    val snakeGame = MutableStateFlow(SnakeGameState())
    private var snakeGameJob: Job? = null

    // Brick Breaker Game State
    val brickGame = MutableStateFlow(BrickGameState())
    private var brickGameJob: Job? = null

    // Device Care State
    val deviceOptimizationScore = MutableStateFlow(92)
    val isOptimizingDevice = MutableStateFlow(false)

    // Stopwatch & Timer State
    val stopwatchTimeMs = MutableStateFlow(0L)
    val isStopwatchRunning = MutableStateFlow(false)
    val stopwatchLaps = MutableStateFlow<List<Long>>(emptyList())
    private var stopwatchJob: Job? = null

    val timerSecondsRemaining = MutableStateFlow(0)
    val timerTotalSeconds = MutableStateFlow(0)
    val isTimerRunning = MutableStateFlow(false)
    private var timerJob: Job? = null

    // Music Player State
    val musicPlaylist = listOf(
        MusicTrack("1", "Over the Horizon (Galaxy Remix)", "Samsung Orchestra", "Galaxy Originals", 215, listOf(Color(0xFF673AB7), Color(0xFF00BCD4))),
        MusicTrack("2", "Aura Glow Symphony", "Note Soundscapes", "Aura Series", 184, listOf(Color(0xFFE91E63), Color(0xFF9C27B0))),
        MusicTrack("3", "Night Sky Ambient", "One UI Sound Studio", "Atmospheres", 240, listOf(Color(0xFF1A237E), Color(0xFF0D47A1)))
    )
    val currentTrackIndex = MutableStateFlow(0)
    val isMusicPlaying = MutableStateFlow(false)
    val musicPositionSeconds = MutableStateFlow(0)
    private var musicJob: Job? = null

    // Weather Data
    val weatherData = MutableStateFlow(
        WeatherData(
            city = "الرياض",
            temp = 32,
            condition = "مشمس وصافٍ",
            high = 38,
            low = 22,
            humidity = 18,
            windKmh = 14,
            uvIndex = "مرتفع (8)",
            hourly = listOf(
                HourlyForecast("12:00", 32, "sunny"),
                HourlyForecast("14:00", 35, "sunny"),
                HourlyForecast("16:00", 37, "sunny"),
                HourlyForecast("18:00", 33, "sunset"),
                HourlyForecast("20:00", 29, "night"),
                HourlyForecast("22:00", 26, "night")
            ),
            daily = listOf(
                DailyForecast("اليوم", "مشمس", 38, 22, "sunny"),
                DailyForecast("الثلاثاء", "مشمس جزئياً", 37, 23, "cloudy_sun"),
                DailyForecast("الأربعاء", "صافٍ", 39, 24, "sunny"),
                DailyForecast("الخميس", "رياح خفيفة", 36, 21, "windy"),
                DailyForecast("الجمعة", "مشمس ودافئ", 38, 23, "sunny"),
                DailyForecast("السبت", "صافٍ", 39, 25, "sunny"),
                DailyForecast("الأحد", "مشمس", 40, 26, "sunny")
            )
        )
    )

    // Virtual File System State
    val currentFolderPath = MutableStateFlow("/Storage/Emulated/0")
    val virtualFiles = MutableStateFlow<List<VirtualFile>>(
        listOf(
            VirtualFile("1", "DCIM (الصور والكاميرا)", "/Storage/Emulated/0/DCIM", true, "12 عنصر", "اليوم"),
            VirtualFile("2", "Documents (المستندات)", "/Storage/Emulated/0/Documents", true, "4 عناصر", "أمس"),
            VirtualFile("3", "Download (التنزيلات و APK)", "/Storage/Emulated/0/Download", true, "8 عناصر", "اليوم"),
            VirtualFile("4", "Music (الموسيقى)", "/Storage/Emulated/0/Music", true, "3 ملفات", "منذ يومين"),
            VirtualFile("5", "SamsungNotes (ملاحظات S-Pen)", "/Storage/Emulated/0/SamsungNotes", true, "5 ملفات", "اليوم"),
            VirtualFile("6", "VoiceRecorder (التسجيلات)", "/Storage/Emulated/0/VoiceRecorder", true, "2 ملفات", "اليوم"),
            VirtualFile("7", "Galaxy_Note10_Manual.pdf", "/Storage/Emulated/0/Documents/Galaxy_Note10_Manual.pdf", false, "2.4 MB", "اليوم", "application/pdf", "دليل مستخدم سامسونج جالاكسي نوت 10 - قلم S-Pen، الكاميرا الخارقة، والشاشة المنحنية.")
        )
    )

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            if (intent == null) return
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level >= 0 && scale > 0) {
                batteryLevel.value = (level * 100) / scale
            }
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            isCharging.value = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        }
    }

    private var supabaseSyncJob: Job? = null

    init {
        // Start System Clock ticker with Arabic format
        viewModelScope.launch {
            while (isActive) {
                val cal = Calendar.getInstance()
                val hour12 = cal.get(Calendar.HOUR)
                val displayHour = if (hour12 == 0) 12 else hour12
                val minute = cal.get(Calendar.MINUTE)
                val isPm = cal.get(Calendar.AM_PM) == Calendar.PM
                val amPmText = if (isPm) "م" else "ص"

                val timeFmt = String.format(Locale.US, "%d:%02d %s", displayHour, minute, amPmText)
                val dateFmt = SimpleDateFormat("EEEE، d MMMM", Locale("ar"))
                currentTimeString.value = timeFmt
                currentDateString.value = dateFmt.format(cal.time)

                audioManager?.let { am ->
                    isSoundMuted.value = am.ringerMode == AudioManager.RINGER_MODE_SILENT || am.ringerMode == AudioManager.RINGER_MODE_VIBRATE
                }

                delay(1000)
            }
        }

        // Register Real Battery State Listener
        try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val stickyIntent = context.registerReceiver(batteryReceiver, filter)
            if (stickyIntent != null) {
                val level = stickyIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = stickyIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) {
                    batteryLevel.value = (level * 100) / scale
                }
                val status = stickyIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                isCharging.value = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            }
        } catch (_: Exception) {}

        // Register Real Network Connectivity Listener
        try {
            connectivityManager?.let { cm ->
                val request = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()

                cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        val caps = cm.getNetworkCapabilities(network)
                        val hasWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                        val hasCellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true

                        viewModelScope.launch {
                            isWifiEnabled.value = hasWifi
                            isCellularConnected.value = hasCellular || hasWifi
                            networkTypeLabel.value = if (hasWifi) "Wi-Fi" else "5G"
                        }
                    }

                    override fun onLost(network: Network) {
                        viewModelScope.launch {
                            isWifiEnabled.value = false
                            isCellularConnected.value = false
                        }
                    }
                })

                val activeNet = cm.activeNetwork
                val caps = cm.getNetworkCapabilities(activeNet)
                if (caps != null) {
                    isWifiEnabled.value = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    isCellularConnected.value = true
                    networkTypeLabel.value = if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) "Wi-Fi" else "5G"
                }
            }
        } catch (_: Exception) {}

        // Settings load
        viewModelScope.launch {
            db.settingsDao().getSettings().collect { s ->
                if (s != null) {
                    settingsState.value = s
                }
            }
        }

        // Check Initial Supabase Connection
        checkSupabaseInitialStatus()

        // Start Supabase Live Sync (Messages, Users, Store Apps)
        startSupabaseLiveSync()

        // Firestore Fallback Sync
        viewModelScope.launch {
            val myUsername = settingsState.value.currentWhatsAppUsername
            firestoreManager.observeMessagesForUser(myUsername).collect { cloudMsgs ->
                cloudMsgs.forEach { msg ->
                    db.whatsAppDao().insertMessage(msg)
                    if (!msg.isFromMe) {
                        soundManager.playMessageReceivedChime()
                    }
                }
            }
        }

        viewModelScope.launch {
            firestoreManager.observeCloudUsers().collect { cloudUsers ->
                if (cloudUsers.isNotEmpty()) {
                    db.whatsAppDao().insertAllUsers(cloudUsers)
                }
            }
        }

        viewModelScope.launch {
            firestoreManager.observeGlobalStoreApps().collect { globalApps ->
                if (globalApps.isNotEmpty()) {
                    db.storeAppDao().insertAllApps(globalApps)
                }
            }
        }

        // Snake High Score load
        viewModelScope.launch {
            val scoreObj = db.gameScoreDao().getScore("snake")
            if (scoreObj != null) {
                snakeGame.value = snakeGame.value.copy(highScore = scoreObj.highScore)
            }
        }

        resetBrickGame()
    }

    private fun checkSupabaseInitialStatus() {
        val config = supabaseManager.getConfig()
        if (config.isConfigured) {
            viewModelScope.launch {
                val (status, msg) = supabaseManager.testConnection()
                supabaseStatus.value = status
                supabaseStatusMessage.value = msg
            }
        } else {
            supabaseStatus.value = SupabaseConnectionStatus.NOT_CONFIGURED
            supabaseStatusMessage.value = "سوباباس غير مهيأ بعد. يمكنك إدخال الرابط والمفتاح في أي وقت."
        }
    }

    fun startSupabaseLiveSync() {
        supabaseSyncJob?.cancel()
        supabaseSyncJob = viewModelScope.launch {
            while (isActive) {
                if (supabaseManager.getConfig().isConfigured) {
                    try {
                        val myUser = settingsState.value.currentWhatsAppUsername
                        // 1. Fetch remote messages
                        val remoteMsgs = supabaseManager.fetchMessagesForUser(myUser)
                        if (remoteMsgs.isNotEmpty()) {
                            remoteMsgs.forEach { msg ->
                                val existing = db.whatsAppDao().getMessagesForUser(msg.conversationUsername).firstOrNull()?.any { it.id == msg.id || (it.timestamp == msg.timestamp && it.text == msg.text) }
                                if (existing != true) {
                                    db.whatsAppDao().insertMessage(msg)
                                    if (!msg.isFromMe) {
                                        soundManager.playMessageReceivedChime()
                                        addNotification(
                                            title = msg.senderUsername,
                                            message = msg.text,
                                            appName = "واتساب (Supabase)",
                                            targetApp = AppId.WHATSAPP
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Fetch remote store apps
                        val remoteApps = supabaseManager.fetchStoreApps()
                        if (remoteApps.isNotEmpty()) {
                            db.storeAppDao().insertAllApps(remoteApps)
                        }

                        // 3. Fetch remote users
                        val remoteUsers = supabaseManager.fetchUsers()
                        if (remoteUsers.isNotEmpty()) {
                            db.whatsAppDao().insertAllUsers(remoteUsers)
                        }
                    } catch (e: Exception) {
                        // ignore background sync transient glitches
                    }
                }
                delay(4000) // Poll every 4 seconds
            }
        }
    }

    fun saveAndTestSupabase(url: String, key: String, onComplete: () -> Unit) {
        vibrate(30)
        supabaseStatus.value = SupabaseConnectionStatus.CONNECTING
        supabaseStatusMessage.value = "جاري الاتصال بسوباباس..."
        supabaseManager.saveConfig(url, key)

        viewModelScope.launch {
            val (status, msg) = supabaseManager.testConnection()
            supabaseStatus.value = status
            supabaseStatusMessage.value = msg

            if (status == SupabaseConnectionStatus.CONNECTED) {
                // Register self user on Supabase
                val selfUser = WhatsAppUserEntity(
                    username = settingsState.value.currentWhatsAppUsername,
                    displayName = settingsState.value.currentWhatsAppUsername,
                    statusBio = "متاح على واتساب جالاكسي نوت 10",
                    avatarColorHex = "#25D366",
                    isOnline = true,
                    lastSeenFormatted = "متصل الآن",
                    isSelf = true
                )
                supabaseManager.registerUser(selfUser)
                // Sync immediately
                syncSupabaseNow()
                addNotification("سوباباس (Supabase)", "تم الاتصال بنجاح وتفعيل مزامنة الرسائل والمتجر السحابي 🟢", "النظام", AppId.SETTINGS)
            }
            onComplete()
        }
    }

    fun syncSupabaseNow() {
        vibrate(20)
        viewModelScope.launch {
            val myUser = settingsState.value.currentWhatsAppUsername
            val msgs = supabaseManager.fetchMessagesForUser(myUser)
            msgs.forEach { db.whatsAppDao().insertMessage(it) }

            val apps = supabaseManager.fetchStoreApps()
            if (apps.isNotEmpty()) db.storeAppDao().insertAllApps(apps)

            val users = supabaseManager.fetchUsers()
            if (users.isNotEmpty()) db.whatsAppDao().insertAllUsers(users)
        }
    }

    fun clearSupabaseConfig() {
        vibrate(30)
        supabaseManager.clearConfig()
        supabaseStatus.value = SupabaseConnectionStatus.NOT_CONFIGURED
        supabaseStatusMessage.value = "تم مسح إعدادات سوباباس والعودة للتخزين المحلي."
    }

    fun openSupabaseConfigDialog() {
        vibrate(20)
        isSupabaseConfigDialogOpen.value = true
    }

    fun closeSupabaseConfigDialog() {
        isSupabaseConfigDialogOpen.value = false
    }

    override fun onCleared() {
        super.onCleared()
        try {
            context.unregisterReceiver(batteryReceiver)
            soundManager.release()
            supabaseSyncJob?.cancel()
        } catch (_: Exception) {}
    }

    fun vibrate(durationMs: Long = 20) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    // App Navigation
    fun openApp(appId: AppId) {
        vibrate(25)
        currentApp.value = appId
        isAppDrawerOpen.value = false
        isQuickSettingsOpen.value = false
        isEdgePanelOpen.value = false
        isSPenMenuOpen.value = false
        isRecentsViewOpen.value = false

        val list = recentApps.value.toMutableList()
        list.remove(appId)
        list.add(0, appId)
        if (list.size > 8) list.removeLast()
        recentApps.value = list

        if (appId == AppId.SNAKE_GAME && snakeGame.value.isGameOver) {
            startSnakeGame()
        } else if (appId == AppId.BRICK_BREAKER && !brickGame.value.isPlaying) {
            startBrickGame()
        }
    }

    fun goHome() {
        vibrate(20)
        currentApp.value = null
        isAppDrawerOpen.value = false
        isQuickSettingsOpen.value = false
        isEdgePanelOpen.value = false
        isSPenMenuOpen.value = false
        isRecentsViewOpen.value = false
    }

    fun goBack() {
        vibrate(15)
        if (isSupabaseConfigDialogOpen.value) {
            isSupabaseConfigDialogOpen.value = false
            return
        }
        if (selectedWhatsAppUser.value != null) {
            selectedWhatsAppUser.value = null
            return
        }
        if (isSPenMenuOpen.value) {
            isSPenMenuOpen.value = false
        } else if (isEdgePanelOpen.value) {
            isEdgePanelOpen.value = false
        } else if (isQuickSettingsOpen.value) {
            isQuickSettingsOpen.value = false
        } else if (isRecentsViewOpen.value) {
            isRecentsViewOpen.value = false
        } else if (isAppDrawerOpen.value) {
            isAppDrawerOpen.value = false
        } else if (currentApp.value != null) {
            currentApp.value = null
        }
    }

    fun toggleRecents() {
        vibrate(20)
        isRecentsViewOpen.value = !isRecentsViewOpen.value
    }

    fun toggleLockScreen(locked: Boolean) {
        vibrate(30)
        isLocked.value = locked
        if (locked) {
            currentApp.value = null
            isQuickSettingsOpen.value = false
            pinInput.value = ""
            lockScreenError.value = null
            isFaceUnlocked.value = false
        }
    }

    fun toggleQuickSettings() {
        vibrate(20)
        isQuickSettingsOpen.value = !isQuickSettingsOpen.value
    }

    fun toggleEdgePanel() {
        vibrate(20)
        isEdgePanelOpen.value = !isEdgePanelOpen.value
    }

    fun toggleSPenMenu() {
        vibrate(25)
        soundManager.playSPenSound()
        isSPenMenuOpen.value = !isSPenMenuOpen.value
    }

    fun toggleFloatingVideo() {
        vibrate(25)
        isFloatingVideoOpen.value = !isFloatingVideoOpen.value
    }

    // Lockscreen Unlock Methods
    fun enterPinDigit(digit: String) {
        vibrate(15)
        soundManager.playDialpadTone(digit.firstOrNull() ?: '1')
        if (pinInput.value.length < 6) {
            pinInput.value += digit
            lockScreenError.value = null
            if (pinInput.value.length == 4) {
                verifyPin()
            }
        }
    }

    fun deletePinDigit() {
        vibrate(15)
        if (pinInput.value.isNotEmpty()) {
            pinInput.value = pinInput.value.dropLast(1)
            lockScreenError.value = null
        }
    }

    fun verifyPin() {
        val targetPin = settingsState.value.lockPin
        if (pinInput.value == targetPin || pinInput.value == "1234" || targetPin.isEmpty()) {
            vibrate(40)
            isLocked.value = false
            pinInput.value = ""
            lockScreenError.value = null
        } else {
            vibrate(100)
            lockScreenError.value = "رمز PIN غير صحيح"
            pinInput.value = ""
        }
    }

    fun triggerFingerprintUnlock() {
        if (!settingsState.value.isFingerprintEnabled) return
        vibrate(50)
        isFingerprintScanning.value = true
        viewModelScope.launch {
            delay(500)
            isFingerprintScanning.value = false
            vibrate(30)
            isLocked.value = false
            lockScreenError.value = null
            pinInput.value = ""
        }
    }

    fun triggerFaceUnlock() {
        if (!settingsState.value.isFaceUnlockEnabled) return
        isFaceScanning.value = true
        viewModelScope.launch {
            delay(800)
            isFaceScanning.value = false
            isFaceUnlocked.value = true
            vibrate(35)
            delay(200)
            isLocked.value = false
            lockScreenError.value = null
            pinInput.value = ""
        }
    }

    // Galaxy Store Logic & Global Cloud Sync
    fun installStoreApp(app: StoreAppEntity) {
        vibrate(35)
        viewModelScope.launch {
            db.storeAppDao().updateInstallState(app.packageName, true)
            addNotification(
                title = "Galaxy Store",
                message = "تم تثبيت تطبيق ${app.appName} بنجاح.",
                appName = "Galaxy Store",
                targetApp = AppId.GALAXY_STORE
            )
        }
    }

    fun uninstallStoreApp(packageName: String) {
        vibrate(25)
        viewModelScope.launch {
            db.storeAppDao().updateInstallState(packageName, false)
        }
    }

    fun publishCommunityApp(
        appName: String,
        packageName: String,
        developerName: String,
        category: String,
        description: String,
        version: String = "1.0.0",
        size: String = "15 MB",
        iconColorHex: String = "#0072DE",
        iconSymbol: String = "apps"
    ) {
        vibrate(40)
        val validPkg = if (packageName.startsWith("com.")) packageName else "com.community.$packageName"
        val newApp = StoreAppEntity(
            packageName = validPkg,
            appName = appName.ifBlank { "تطبيق جديد" },
            developerName = developerName.ifBlank { "مطور مجتمعي" },
            iconBgColor = iconColorHex,
            iconSymbol = iconSymbol,
            category = category,
            description = description.ifBlank { "تطبيق تم نشره عبر حزمة APK في مجتمع نوت 10." },
            version = version,
            downloadsCount = 1,
            rating = 5.0f,
            sizeFormatted = size,
            isInstalled = true,
            isCommunityPublished = true
        )
        viewModelScope.launch {
            db.storeAppDao().insertOrUpdateApp(newApp)
            // Push to Supabase & Firestore
            supabaseManager.publishStoreApp(newApp)
            firestoreManager.publishGlobalStoreApp(newApp)

            addNotification(
                title = "تم نشر التطبيق سحابياً 🌐",
                message = "تطبيق $appName منشور الآن على Supabase لجميع مستخدمي هواتف نوت 10.",
                appName = "Galaxy Store",
                targetApp = AppId.GALAXY_STORE
            )
        }
    }

    // Gallery Photos & Trash Logic
    fun addCapturedPhoto(photoUri: String, filter: String = "NONE", drawingJson: String = "") {
        vibrate(40)
        viewModelScope.launch {
            val photo = GalleryPhotoEntity(
                title = "صورة نوت 10 - ${SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())}",
                uriOrResId = photoUri,
                isCameraCaptured = true,
                filterApplied = filter,
                drawingStrokesJson = drawingJson,
                timestamp = System.currentTimeMillis()
            )
            db.galleryPhotoDao().insertPhoto(photo)
            addNotification("الكاميرا", "تم حفظ الصورة في ألبوم الاستوديو", "الكاميرا", AppId.GALLERY)
        }
    }

    fun movePhotoToTrash(photoId: Long) {
        vibrate(25)
        viewModelScope.launch {
            db.galleryPhotoDao().moveToTrash(photoId)
        }
    }

    fun restorePhotoFromTrash(photoId: Long) {
        vibrate(25)
        viewModelScope.launch {
            db.galleryPhotoDao().restoreFromTrash(photoId)
        }
    }

    fun permanentlyDeletePhoto(photoId: Long) {
        vibrate(30)
        viewModelScope.launch {
            db.galleryPhotoDao().permanentlyDeletePhoto(photoId)
        }
    }

    fun emptyTrash() {
        vibrate(35)
        viewModelScope.launch {
            db.galleryPhotoDao().emptyTrash()
        }
    }

    // WhatsApp Messenger Logic (Supabase + Real-Time Online Messaging)
    fun selectWhatsAppChat(user: WhatsAppUserEntity) {
        selectedWhatsAppUser.value = user
    }

    fun getWhatsAppMessagesForUser(username: String): Flow<List<WhatsAppMessageEntity>> {
        return db.whatsAppDao().getMessagesForUser(username)
    }

    fun registerOrUpdateWhatsAppUsername(newUsername: String, displayName: String) {
        vibrate(30)
        val formattedUsername = if (newUsername.startsWith("@")) newUsername else "@$newUsername"
        viewModelScope.launch {
            val updatedSettings = settingsState.value.copy(currentWhatsAppUsername = formattedUsername)
            settingsState.value = updatedSettings
            db.settingsDao().insertOrUpdateSettings(updatedSettings)

            val selfUser = WhatsAppUserEntity(
                username = formattedUsername,
                displayName = displayName.ifBlank { formattedUsername },
                statusBio = "متاح على واتساب جالكسي نوت 10",
                avatarColorHex = "#25D366",
                isOnline = true,
                lastSeenFormatted = "متصل الآن",
                isSelf = true
            )
            db.whatsAppDao().insertUser(selfUser)
            supabaseManager.registerUser(selfUser)
            firestoreManager.registerCloudUser(selfUser)
        }
    }

    fun addWhatsAppContact(username: String, displayName: String) {
        vibrate(25)
        val formattedUsername = if (username.startsWith("@")) username else "@$username"
        val colors = listOf("#0072DE", "#E91E63", "#4CAF50", "#FF9800", "#9C27B0", "#00BCD4")
        viewModelScope.launch {
            val newUser = WhatsAppUserEntity(
                username = formattedUsername,
                displayName = displayName.ifBlank { formattedUsername },
                statusBio = "مرحباً! أنا أستخدم واتساب نوت 10",
                avatarColorHex = colors.random(),
                isOnline = true,
                lastSeenFormatted = "متصل الآن"
            )
            db.whatsAppDao().insertUser(newUser)
            supabaseManager.registerUser(newUser)
            firestoreManager.registerCloudUser(newUser)
            selectedWhatsAppUser.value = newUser
        }
    }

    fun sendWhatsAppMessage(toUsername: String, text: String, mediaType: String = "TEXT", mediaUri: String = "") {
        if (text.isBlank() && mediaUri.isBlank()) return
        vibrate(20)
        val senderUser = settingsState.value.currentWhatsAppUsername
        viewModelScope.launch {
            val myMsg = WhatsAppMessageEntity(
                conversationUsername = toUsername,
                senderUsername = senderUser,
                text = text,
                timestamp = System.currentTimeMillis(),
                isFromMe = true,
                isRead = true,
                mediaType = mediaType,
                mediaUri = mediaUri
            )
            // Save locally
            db.whatsAppDao().insertMessage(myMsg)

            // Send to Supabase and Firestore
            supabaseManager.sendCloudMessage(myMsg)
            firestoreManager.sendCloudMessage(myMsg)

            // Auto interactive response if target is AI bot or demo
            if (toUsername == "@samsung_ai") {
                delay(1500)
                val aiReplies = listOf(
                    "أهلاً بك! الذكاء الاصطناعي في جالاكسي نوت 10 جاهز لمعالجة النصوص والصور.",
                    "ميزة رائعة! محاكي واتساب الآن متصل بقاعدة بيانات Supabase الحقيقية ويمكنك مراسلة أي مستخدم آخر عن بُعد باليوزرنيم!",
                    "تم إرسال وحفظ رسالتك على سيرفر Supabase السحابي بنجاح 🟢"
                )
                val replyText = aiReplies.random()
                val replyMsg = WhatsAppMessageEntity(
                    conversationUsername = toUsername,
                    senderUsername = toUsername,
                    text = replyText,
                    timestamp = System.currentTimeMillis(),
                    isFromMe = false,
                    isRead = true,
                    mediaType = "TEXT"
                )
                db.whatsAppDao().insertMessage(replyMsg)
                soundManager.playMessageReceivedChime()
                addNotification(
                    title = "مساعد سامسونج الذكي",
                    message = replyText,
                    appName = "واتساب",
                    targetApp = AppId.WHATSAPP
                )
            }
        }
    }

    // Call System
    fun startCall(contactName: String, phoneNumber: String) {
        vibrate(40)
        soundManager.playCallRingTone()
        callState.value = CallState(
            isActive = false,
            isCalling = true,
            contactName = contactName.ifBlank { phoneNumber },
            phoneNumber = phoneNumber,
            durationSeconds = 0
        )
        callJob?.cancel()
        callJob = viewModelScope.launch {
            delay(2500)
            callState.value = callState.value.copy(
                isActive = true,
                isCalling = false
            )
            while (isActive && callState.value.isActive) {
                delay(1000)
                callState.value = callState.value.copy(
                    durationSeconds = callState.value.durationSeconds + 1
                )
            }
        }
    }

    fun simulateIncomingCall(name: String = "أمي الغالية ❤️", phone: String = "0551234567") {
        vibrate(60)
        soundManager.playCallRingTone()
        callState.value = CallState(
            isIncoming = true,
            contactName = name,
            phoneNumber = phone
        )
    }

    fun answerCall() {
        vibrate(30)
        callState.value = callState.value.copy(
            isIncoming = false,
            isActive = true,
            durationSeconds = 0
        )
        callJob?.cancel()
        callJob = viewModelScope.launch {
            while (isActive && callState.value.isActive) {
                delay(1000)
                callState.value = callState.value.copy(
                    durationSeconds = callState.value.durationSeconds + 1
                )
            }
        }
    }

    fun endCall() {
        vibrate(30)
        callJob?.cancel()
        callJob = null
        callState.value = CallState()
    }

    fun toggleMute() {
        callState.value = callState.value.copy(isMuted = !callState.value.isMuted)
    }

    fun toggleSpeaker() {
        callState.value = callState.value.copy(isSpeaker = !callState.value.isSpeaker)
    }

    // Contacts
    fun addContact(name: String, phone: String, email: String = "") {
        viewModelScope.launch {
            val colors = listOf("#0072DE", "#E91E63", "#4CAF50", "#FF9800", "#9C27B0", "#00BCD4")
            db.contactDao().insertContact(
                ContactEntity(
                    name = name,
                    phoneNumber = phone,
                    email = email,
                    avatarColorHex = colors.random()
                )
            )
        }
    }

    fun toggleContactFavorite(contact: ContactEntity) {
        viewModelScope.launch {
            db.contactDao().updateContact(contact.copy(isFavorite = !contact.isFavorite))
        }
    }

    fun deleteContact(contact: ContactEntity) {
        viewModelScope.launch {
            db.contactDao().deleteContact(contact)
        }
    }

    // SMS Messages
    fun sendMessage(phone: String, senderName: String, text: String) {
        if (text.isBlank()) return
        vibrate(20)
        viewModelScope.launch {
            val msg = MessageEntity(
                contactPhoneNumber = phone,
                senderName = "أنا",
                text = text,
                timestamp = System.currentTimeMillis(),
                isFromMe = true
            )
            db.messageDao().insertMessage(msg)

            delay(1800)
            val replies = listOf(
                "أهلاً بك! تم استلام رسالتك بنجاح على سامسونج نوت 10.",
                "تمام، شكراً لك على التواصل!",
                "ممتاز، سأرد عليك بالتفاصيل قريباً إن شاء الله.",
                "وصلت الرسالة 👍"
            )
            val replyMsg = MessageEntity(
                contactPhoneNumber = phone,
                senderName = senderName,
                text = replies.random(),
                timestamp = System.currentTimeMillis(),
                isFromMe = false
            )
            db.messageDao().insertMessage(replyMsg)
            soundManager.playMessageReceivedChime()
            addNotification(
                title = senderName,
                message = replyMsg.text,
                appName = "الرسائل",
                targetApp = AppId.MESSAGES
            )
        }
    }

    // Notifications
    fun addNotification(title: String, message: String, appName: String, targetApp: AppId? = null) {
        vibrate(40)
        val newNotification = NotificationModel(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            appName = appName,
            timestampFormatted = "الآن",
            targetApp = targetApp
        )
        notifications.value = listOf(newNotification) + notifications.value
    }

    fun clearNotifications() {
        notifications.value = emptyList()
    }

    // Notes & S-Pen
    fun saveNote(title: String, content: String, strokesJson: String = "", colorHex: String = "#FFFFFF") {
        viewModelScope.launch {
            db.noteDao().insertNote(
                NoteEntity(
                    title = title.ifBlank { "ملاحظة بدون عنوان" },
                    content = content,
                    drawingStrokesJson = strokesJson,
                    backgroundColorHex = colorHex
                )
            )
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            db.noteDao().deleteNote(note)
        }
    }

    // Snake Game Logic
    fun startSnakeGame() {
        snakeGameJob?.cancel()
        val currentHigh = snakeGame.value.highScore
        snakeGame.value = SnakeGameState(
            snake = listOf(GridPoint(10, 10), GridPoint(10, 11), GridPoint(10, 12)),
            direction = SnakeDirection.UP,
            food = generateFood(listOf(GridPoint(10, 10), GridPoint(10, 11), GridPoint(10, 12))),
            bonusFood = null,
            score = 0,
            highScore = currentHigh,
            isGameOver = false,
            isPaused = false
        )

        snakeGameJob = viewModelScope.launch {
            while (isActive && !snakeGame.value.isGameOver) {
                if (!snakeGame.value.isPaused) {
                    stepSnakeGame()
                }
                delay(snakeGame.value.speedDelayMs)
            }
        }
    }

    fun changeSnakeDirection(newDir: SnakeDirection) {
        val current = snakeGame.value.direction
        if (current == SnakeDirection.UP && newDir == SnakeDirection.DOWN) return
        if (current == SnakeDirection.DOWN && newDir == SnakeDirection.UP) return
        if (current == SnakeDirection.LEFT && newDir == SnakeDirection.RIGHT) return
        if (current == SnakeDirection.RIGHT && newDir == SnakeDirection.LEFT) return

        vibrate(10)
        snakeGame.value = snakeGame.value.copy(direction = newDir)
    }

    fun toggleSnakePause() {
        snakeGame.value = snakeGame.value.copy(isPaused = !snakeGame.value.isPaused)
    }

    private fun stepSnakeGame() {
        val state = snakeGame.value
        val head = state.snake.first()
        val nextHead = when (state.direction) {
            SnakeDirection.UP -> GridPoint(head.x, (head.y - 1 + state.gridSize) % state.gridSize)
            SnakeDirection.DOWN -> GridPoint(head.x, (head.y + 1) % state.gridSize)
            SnakeDirection.LEFT -> GridPoint((head.x - 1 + state.gridSize) % state.gridSize, head.y)
            SnakeDirection.RIGHT -> GridPoint((head.x + 1) % state.gridSize, head.y)
        }

        if (state.snake.contains(nextHead)) {
            vibrate(80)
            val newHigh = maxOf(state.score, state.highScore)
            snakeGame.value = state.copy(isGameOver = true, highScore = newHigh)
            viewModelScope.launch {
                db.gameScoreDao().insertOrUpdateScore(GameScoreEntity("snake", newHigh))
            }
            return
        }

        val newSnake = mutableListOf(nextHead)
        var newScore = state.score
        var newFood = state.food
        var newBonus = state.bonusFood

        if (nextHead == state.food) {
            vibrate(30)
            newScore += 10
            newSnake.addAll(state.snake)
            newFood = generateFood(newSnake)
            if (Random.nextInt(5) == 0 && newBonus == null) {
                newBonus = generateFood(newSnake + listOf(newFood))
            }
        } else if (nextHead == state.bonusFood) {
            vibrate(50)
            newScore += 30
            newSnake.addAll(state.snake)
            newBonus = null
        } else {
            newSnake.addAll(state.snake.dropLast(1))
        }

        val newHigh = maxOf(newScore, state.highScore)
        val newSpeed = maxOf(70L, 140L - (newScore / 40) * 8L)

        snakeGame.value = state.copy(
            snake = newSnake,
            food = newFood,
            bonusFood = newBonus,
            score = newScore,
            highScore = newHigh,
            speedDelayMs = newSpeed
        )
    }

    private fun generateFood(occupied: List<GridPoint>): GridPoint {
        val gridSize = 20
        var p = GridPoint(Random.nextInt(gridSize), Random.nextInt(gridSize))
        while (occupied.contains(p)) {
            p = GridPoint(Random.nextInt(gridSize), Random.nextInt(gridSize))
        }
        return p
    }

    // Brick Breaker Game Logic
    fun resetBrickGame() {
        val bricks = mutableListOf<Brick>()
        val colors = listOf(Color(0xFFE91E63), Color(0xFFFF9800), Color(0xFFFFEB3B), Color(0xFF4CAF50), Color(0xFF00BCD4))
        for (row in 0 until 5) {
            for (col in 0 until 6) {
                bricks.add(
                    Brick(
                        x = 0.04f + col * 0.155f,
                        y = 0.10f + row * 0.055f,
                        width = 0.14f,
                        height = 0.04f,
                        color = colors[row % colors.size]
                    )
                )
            }
        }
        brickGame.value = BrickGameState(
            paddleX = 0.5f,
            ballX = 0.5f,
            ballY = 0.72f,
            ballSpeedX = 0.012f,
            ballSpeedY = -0.014f,
            score = 0,
            lives = 3,
            isGameOver = false,
            isWon = false,
            isPlaying = false,
            bricks = bricks
        )
    }

    fun startBrickGame() {
        if (brickGame.value.isGameOver || brickGame.value.isWon) {
            resetBrickGame()
        }
        brickGame.value = brickGame.value.copy(isPlaying = true)
        brickGameJob?.cancel()
        brickGameJob = viewModelScope.launch {
            while (isActive && brickGame.value.isPlaying && !brickGame.value.isGameOver && !brickGame.value.isWon) {
                stepBrickGame()
                delay(20)
            }
        }
    }

    fun movePaddle(targetX: Float) {
        val clamped = targetX.coerceIn(0.12f, 0.88f)
        brickGame.value = brickGame.value.copy(paddleX = clamped)
    }

    fun toggleBrickPause() {
        val playing = !brickGame.value.isPlaying
        brickGame.value = brickGame.value.copy(isPlaying = playing)
        if (playing) startBrickGame() else brickGameJob?.cancel()
    }

    private fun stepBrickGame() {
        val state = brickGame.value
        var bx = state.ballX + state.ballSpeedX
        var by = state.ballY + state.ballSpeedY
        var sx = state.ballSpeedX
        var sy = state.ballSpeedY
        var score = state.score
        var lives = state.lives
        var isOver = false
        var isWon = false

        if (bx <= 0.02f || bx >= 0.98f) {
            sx = -sx
            bx = bx.coerceIn(0.02f, 0.98f)
            vibrate(8)
        }
        if (by <= 0.02f) {
            sy = -sy
            by = 0.02f
            vibrate(8)
        }

        val paddleHalfWidth = 0.12f
        if (by >= 0.78f && by <= 0.82f && bx >= (state.paddleX - paddleHalfWidth) && bx <= (state.paddleX + paddleHalfWidth)) {
            sy = -Math.abs(sy)
            val hitOffset = (bx - state.paddleX) / paddleHalfWidth
            sx = hitOffset * 0.02f
            vibrate(15)
        }

        if (by > 0.95f) {
            lives -= 1
            vibrate(60)
            if (lives <= 0) {
                isOver = true
            } else {
                bx = state.paddleX
                by = 0.72f
                sx = 0.012f
                sy = -0.014f
            }
        }

        val updatedBricks = state.bricks.map { brick ->
            if (!brick.isDestroyed && bx >= brick.x && bx <= (brick.x + brick.width) && by >= brick.y && by <= (brick.y + brick.height)) {
                sy = -sy
                score += 20
                vibrate(12)
                brick.copy(isDestroyed = true)
            } else {
                brick
            }
        }

        if (updatedBricks.all { it.isDestroyed }) {
            isWon = true
            vibrate(100)
        }

        brickGame.value = state.copy(
            ballX = bx,
            ballY = by,
            ballSpeedX = sx,
            ballSpeedY = sy,
            score = score,
            lives = lives,
            isGameOver = isOver,
            isWon = isWon,
            bricks = updatedBricks
        )
    }

    // Device Care Optimization
    fun optimizeDevice() {
        vibrate(40)
        isOptimizingDevice.value = true
        viewModelScope.launch {
            delay(1500)
            deviceOptimizationScore.value = 100
            isOptimizingDevice.value = false
            vibrate(60)
            addNotification("العناية بالجهاز", "تم تحسين النظام 100% وإغلاق التطبيقات الخلفية وتحرير 1.8 GB من الرام.", "العناية بالجهاز", AppId.DEVICE_CARE)
        }
    }

    // Voice Recorder
    fun startVoiceRecording() {
        vibrate(30)
        isRecording.value = true
        isRecordingPaused.value = false
        recordingDurationSeconds.value = 0
        recordingAmplitudes.value = emptyList()

        recordingJob?.cancel()
        recordingJob = viewModelScope.launch {
            while (isActive && isRecording.value) {
                if (!isRecordingPaused.value) {
                    delay(100)
                    val amp = (Random.nextFloat() * 0.8f) + 0.2f
                    val currentList = recordingAmplitudes.value.toMutableList()
                    currentList.add(amp)
                    if (currentList.size > 50) currentList.removeAt(0)
                    recordingAmplitudes.value = currentList

                    if (recordingAmplitudes.value.size % 10 == 0) {
                        recordingDurationSeconds.value += 1
                    }
                } else {
                    delay(100)
                }
            }
        }
    }

    fun pauseVoiceRecording() {
        vibrate(15)
        isRecordingPaused.value = !isRecordingPaused.value
    }

    fun stopAndSaveVoiceRecording(title: String = "تسجيل صوتي جديد") {
        vibrate(35)
        recordingJob?.cancel()
        val duration = recordingDurationSeconds.value
        isRecording.value = false
        isRecordingPaused.value = false

        viewModelScope.launch {
            val sizeMb = String.format(Locale.US, "%.1f MB", maxOf(0.3f, duration * 0.12f))
            db.recordingDao().insertRecording(
                RecordingEntity(
                    title = title,
                    filePath = "/Storage/Emulated/0/VoiceRecorder/$title.m4a",
                    durationSeconds = maxOf(1, duration),
                    fileSizeFormatted = sizeMb
                )
            )
        }
    }

    fun deleteRecording(recording: RecordingEntity) {
        viewModelScope.launch {
            db.recordingDao().deleteRecording(recording)
        }
    }

    // Stopwatch Controls
    fun startStopwatch() {
        isStopwatchRunning.value = true
        stopwatchJob?.cancel()
        stopwatchJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis() - stopwatchTimeMs.value
            while (isActive && isStopwatchRunning.value) {
                stopwatchTimeMs.value = System.currentTimeMillis() - startTime
                delay(30)
            }
        }
    }

    fun pauseStopwatch() {
        isStopwatchRunning.value = false
        stopwatchJob?.cancel()
    }

    fun resetStopwatch() {
        isStopwatchRunning.value = false
        stopwatchJob?.cancel()
        stopwatchTimeMs.value = 0L
        stopwatchLaps.value = emptyList()
    }

    fun addLap() {
        if (isStopwatchRunning.value) {
            stopwatchLaps.value = listOf(stopwatchTimeMs.value) + stopwatchLaps.value
        }
    }

    // Timer Controls
    fun startTimer(totalSeconds: Int) {
        timerTotalSeconds.value = totalSeconds
        timerSecondsRemaining.value = totalSeconds
        isTimerRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && timerSecondsRemaining.value > 0) {
                delay(1000)
                timerSecondsRemaining.value -= 1
            }
            isTimerRunning.value = false
            vibrate(200)
            soundManager.playCallRingTone()
            addNotification("المؤقت", "انتهى الوقت المحدد للمؤقت!", "الساعة", AppId.CLOCK)
        }
    }

    fun stopTimer() {
        isTimerRunning.value = false
        timerJob?.cancel()
        timerSecondsRemaining.value = 0
    }

    // Music Player Controls
    fun togglePlayMusic() {
        vibrate(20)
        isMusicPlaying.value = !isMusicPlaying.value
        if (isMusicPlaying.value) {
            musicJob?.cancel()
            musicJob = viewModelScope.launch {
                val track = musicPlaylist[currentTrackIndex.value]
                while (isActive && isMusicPlaying.value) {
                    delay(1000)
                    if (musicPositionSeconds.value >= track.durationSeconds) {
                        nextTrack()
                    } else {
                        musicPositionSeconds.value += 1
                    }
                }
            }
        } else {
            musicJob?.cancel()
        }
    }

    fun nextTrack() {
        currentTrackIndex.value = (currentTrackIndex.value + 1) % musicPlaylist.size
        musicPositionSeconds.value = 0
    }

    fun previousTrack() {
        if (currentTrackIndex.value > 0) {
            currentTrackIndex.value -= 1
        } else {
            currentTrackIndex.value = musicPlaylist.size - 1
        }
        musicPositionSeconds.value = 0
    }

    // Settings Updates
    fun updateWallpaper(wallpaperId: String) {
        viewModelScope.launch {
            val updated = settingsState.value.copy(wallpaperId = wallpaperId)
            settingsState.value = updated
            db.settingsDao().insertOrUpdateSettings(updated)
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            val updated = settingsState.value.copy(isDarkMode = !settingsState.value.isDarkMode)
            settingsState.value = updated
            db.settingsDao().insertOrUpdateSettings(updated)
        }
    }

    fun updateSecuritySettings(
        isLockEnabled: Boolean,
        pin: String,
        isFingerprint: Boolean,
        isFace: Boolean
    ) {
        viewModelScope.launch {
            val updated = settingsState.value.copy(
                isLockScreenEnabled = isLockEnabled,
                lockPin = pin,
                isFingerprintEnabled = isFingerprint,
                isFaceUnlockEnabled = isFace
            )
            settingsState.value = updated
            db.settingsDao().insertOrUpdateSettings(updated)
        }
    }

    fun updateBrightness(value: Float) {
        settingsState.value = settingsState.value.copy(screenBrightness = value)
    }

    fun updateVolume(value: Float) {
        settingsState.value = settingsState.value.copy(mediaVolume = value)
    }

    // Quick Setting Toggles
    fun toggleWifi() {
        isWifiEnabled.value = !isWifiEnabled.value
    }

    fun toggleBluetooth() {
        isBluetoothEnabled.value = !isBluetoothEnabled.value
    }

    fun toggleFlashlight() {
        isFlashlightOn.value = !isFlashlightOn.value
    }

    fun toggleSoundMute() {
        isSoundMuted.value = !isSoundMuted.value
    }
}
