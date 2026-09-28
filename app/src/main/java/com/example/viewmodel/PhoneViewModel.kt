package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import com.example.data.model.*
import com.example.ui.theme.*
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

    // System Navigation State
    val isLocked = MutableStateFlow(false)
    val currentApp = MutableStateFlow<AppId?>(null)
    val isAppDrawerOpen = MutableStateFlow(false)
    val isQuickSettingsOpen = MutableStateFlow(false)
    val isEdgePanelOpen = MutableStateFlow(false)
    val isSPenMenuOpen = MutableStateFlow(false)
    val recentApps = MutableStateFlow<List<AppId>>(emptyList())
    val isRecentsViewOpen = MutableStateFlow(false)

    // Device Settings State
    val settingsState = MutableStateFlow(
        AppSettingsEntity(
            wallpaperId = "aura_glow",
            isDarkMode = true,
            isLockScreenEnabled = false,
            screenBrightness = 0.9f,
            mediaVolume = 0.85f,
            sPenSoundEnabled = true,
            edgePanelEnabled = true
        )
    )

    // Live Time & Battery State
    val currentTimeString = MutableStateFlow("12:00")
    val currentDateString = MutableStateFlow("الاثنين، ٢٨ سبتمبر")
    val batteryLevel = MutableStateFlow(92)
    val isCharging = MutableStateFlow(false)
    val isWifiEnabled = MutableStateFlow(true)
    val isBluetoothEnabled = MutableStateFlow(true)
    val isFlashlightOn = MutableStateFlow(false)
    val isSoundMuted = MutableStateFlow(false)

    // Call System State
    val callState = MutableStateFlow(CallState())
    private var callJob: Job? = null

    // Notifications State
    val notifications = MutableStateFlow<List<NotificationModel>>(
        listOf(
            NotificationModel(
                id = "n_welcome",
                title = "Samsung Note 10",
                message = "مرحباً بك في هاتف جالاكسي نوت 10. اسحب لأسفل لفتح لوحة الإشعارات.",
                appName = "النظام",
                timestampFormatted = "الآن",
                targetApp = AppId.SETTINGS
            )
        )
    )

    // Contacts & Messages from DB
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

    // Gallery Photos State
    val galleryPhotos = MutableStateFlow<List<String>>(emptyList())

    // Voice Recorder State
    val isRecording = MutableStateFlow(false)
    val isRecordingPaused = MutableStateFlow(false)
    val recordingDurationSeconds = MutableStateFlow(0)
    val recordingAmplitudes = MutableStateFlow<List<Float>>(emptyList())
    private var mediaRecorder: MediaRecorder? = null
    private var currentRecordingFile: File? = null
    private var recordingJob: Job? = null

    // Snake Game State
    val snakeGame = MutableStateFlow(SnakeGameState())
    private var snakeGameJob: Job? = null

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
            VirtualFile("3", "Download (التنزيلات)", "/Storage/Emulated/0/Download", true, "8 عناصر", "اليوم"),
            VirtualFile("4", "Music (الموسيقى)", "/Storage/Emulated/0/Music", true, "3 ملفات", "منذ يومين"),
            VirtualFile("5", "SamsungNotes (ملاحظات S-Pen)", "/Storage/Emulated/0/SamsungNotes", true, "5 ملفات", "اليوم"),
            VirtualFile("6", "VoiceRecorder (التسجيلات)", "/Storage/Emulated/0/VoiceRecorder", true, "2 ملفات", "اليوم"),
            VirtualFile("7", "Galaxy_Note10_Manual.pdf", "/Storage/Emulated/0/Documents/Galaxy_Note10_Manual.pdf", false, "2.4 MB", "اليوم", "application/pdf", "دليل مستخدم سامسونج جالاكسي نوت 10 - قلم S-Pen، الكاميرا الخارقة، والشاشة المنحنية."),
            VirtualFile("8", "Welcome_Note.txt", "/Storage/Emulated/0/Documents/Welcome_Note.txt", false, "1.1 KB", "اليوم", "text/plain", "أهلاً بك في نظام تشغيل نوت 10 المحاكي! جميع التطبيقات تعمل بكفاءة.")
        )
    )

    // All Apps list in System
    val allAppsList = listOf(
        AppItem(AppId.PHONE, "الهاتف", "phone", PhoneGreen),
        AppItem(AppId.MESSAGES, "الرسائل", "message", MessagesBlue),
        AppItem(AppId.SAMSUNG_NOTES, "ملاحظات S-Pen", "note", NotesYellow),
        AppItem(AppId.CAMERA, "الكاميرا", "camera", CameraRed),
        AppItem(AppId.GALLERY, "الاستوديو", "gallery", GalleryPurple),
        AppItem(AppId.VOICE_RECORDER, "مسجل الصوت", "mic", RecorderPink),
        AppItem(AppId.FILE_MANAGER, "ملفاتي", "folder", FilesOrange),
        AppItem(AppId.SNAKE_GAME, "لعبة الدودة", "game", SnakeGreen),
        AppItem(AppId.CALCULATOR, "الحاسبة", "calc", CalculatorTeal),
        AppItem(AppId.CLOCK, "الساعة", "clock", ClockNavy),
        AppItem(AppId.SETTINGS, "الضبط", "settings", SettingsSlate),
        AppItem(AppId.BROWSER, "الإنترنت", "web", InternetIndigo),
        AppItem(AppId.MUSIC, "الموسيقى", "music", MusicPink),
        AppItem(AppId.WEATHER, "الطقس", "weather", WeatherSky)
    )

    init {
        // Start time updater coroutine
        viewModelScope.launch {
            while (isActive) {
                val cal = Calendar.getInstance()
                val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
                val dateFmt = SimpleDateFormat("EEEE، d MMMM", Locale("ar"))
                currentTimeString.value = timeFmt.format(cal.time)
                currentDateString.value = dateFmt.format(cal.time)
                delay(1000)
            }
        }

        // Load settings from db
        viewModelScope.launch {
            db.settingsDao().getSettings().collect { s ->
                if (s != null) {
                    settingsState.value = s
                }
            }
        }

        // Load snake game high score
        viewModelScope.launch {
            val scoreObj = db.gameScoreDao().getScore("snake")
            if (scoreObj != null) {
                snakeGame.value = snakeGame.value.copy(highScore = scoreObj.highScore)
            }
        }
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

        // Update recents
        val list = recentApps.value.toMutableList()
        list.remove(appId)
        list.add(0, appId)
        if (list.size > 8) list.removeLast()
        recentApps.value = list

        if (appId == AppId.SNAKE_GAME && snakeGame.value.isGameOver) {
            startSnakeGame()
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
        isSPenMenuOpen.value = !isSPenMenuOpen.value
    }

    // Call System
    fun startCall(contactName: String, phoneNumber: String) {
        vibrate(40)
        callState.value = CallState(
            isActive = false,
            isCalling = true,
            contactName = contactName.ifBlank { phoneNumber },
            phoneNumber = phoneNumber,
            durationSeconds = 0
        )
        callJob?.cancel()
        callJob = viewModelScope.launch {
            delay(2500) // Simulated connection
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
            val randomColor = colors.random()
            db.contactDao().insertContact(
                ContactEntity(
                    name = name,
                    phoneNumber = phone,
                    email = email,
                    avatarColorHex = randomColor
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

    // Messages
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

            // Auto-reply simulation after delay
            delay(1800)
            val replies = listOf(
                "أهلاً بك! تم استلام رسالتك بنجاح على سامسونج نوت 10.",
                "تمام، شكراً لك على التواصل!",
                "ممتاز، سأرد عليك بالتفاصيل قريباً إن شاء الله.",
                "وصلت الرسالة 👍",
                "أنا مشغول حالياً وسأتصل بك لاحقاً."
            )
            val replyMsg = MessageEntity(
                contactPhoneNumber = phone,
                senderName = senderName,
                text = replies.random(),
                timestamp = System.currentTimeMillis(),
                isFromMe = false
            )
            db.messageDao().insertMessage(replyMsg)
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

        // Check self collision
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

    // Camera Photo Save
    fun addPhotoToGallery(photoUri: String) {
        vibrate(40)
        galleryPhotos.value = listOf(photoUri) + galleryPhotos.value
        addNotification("الكاميرا", "تم حفظ صورة جديدة في الاستوديو", "الكاميرا", AppId.GALLERY)
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

    fun toggleLockScreenSetting() {
        viewModelScope.launch {
            val updated = settingsState.value.copy(isLockScreenEnabled = !settingsState.value.isLockScreenEnabled)
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
