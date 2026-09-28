package com.example.data.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppId {
    PHONE,
    MESSAGES,
    SAMSUNG_NOTES,
    CAMERA,
    VOICE_RECORDER,
    GALLERY,
    FILE_MANAGER,
    SNAKE_GAME,
    CALCULATOR,
    CLOCK,
    SETTINGS,
    BROWSER,
    MUSIC,
    WEATHER
}

data class AppItem(
    val id: AppId,
    val name: String,
    val iconName: String,
    val iconBgColor: Color,
    val isSystemApp: Boolean = true,
    val notificationCount: Int = 0
)

data class CallState(
    val isActive: Boolean = false,
    val isIncoming: Boolean = false,
    val isCalling: Boolean = false,
    val contactName: String = "",
    val phoneNumber: String = "",
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeaker: Boolean = false,
    val isHold: Boolean = false
)

data class NotificationModel(
    val id: String,
    val title: String,
    val message: String,
    val appName: String,
    val timestampFormatted: String,
    val targetApp: AppId? = null
)

data class VirtualFile(
    val id: String,
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeFormatted: String = "",
    val lastModified: String = "",
    val mimeType: String = "text/plain",
    val content: String = ""
)

// S-Pen Drawing Canvas Models
data class StrokePoint(val x: Float, val y: Float)

data class DrawingStroke(
    val points: List<StrokePoint>,
    val color: Long,
    val width: Float,
    val isEraser: Boolean = false
)

// Snake Game Models
enum class SnakeDirection { UP, DOWN, LEFT, RIGHT }
data class GridPoint(val x: Int, val y: Int)

data class SnakeGameState(
    val snake: List<GridPoint> = listOf(GridPoint(10, 10), GridPoint(10, 11), GridPoint(10, 12)),
    val direction: SnakeDirection = SnakeDirection.UP,
    val food: GridPoint = GridPoint(5, 5),
    val bonusFood: GridPoint? = null,
    val score: Int = 0,
    val highScore: Int = 0,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val speedDelayMs: Long = 140L,
    val gridSize: Int = 20
)

// Weather Models
data class HourlyForecast(val time: String, val temp: Int, val iconName: String)
data class DailyForecast(val day: String, val condition: String, val maxTemp: Int, val minTemp: Int, val iconName: String)

data class WeatherData(
    val city: String,
    val temp: Int,
    val condition: String,
    val high: Int,
    val low: Int,
    val humidity: Int,
    val windKmh: Int,
    val uvIndex: String,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>
)

// Music Track
data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSeconds: Int,
    val coverGradient: List<Color>
)
