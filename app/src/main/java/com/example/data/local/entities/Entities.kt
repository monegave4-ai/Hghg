package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val email: String = "",
    val avatarColorHex: String = "#4285F4",
    val isFavorite: Boolean = false,
    val lastCallTimestamp: Long = 0L
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactPhoneNumber: String,
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromMe: Boolean = true,
    val isRead: Boolean = true
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String = "",
    val drawingStrokesJson: String = "", // Serialized vector drawing strokes
    val backgroundColorHex: String = "#FFFFFF",
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)

@Entity(tableName = "recordings")
data class RecordingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val filePath: String,
    val durationSeconds: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val fileSizeFormatted: String = "1.2 MB"
)

@Entity(tableName = "game_scores")
data class GameScoreEntity(
    @PrimaryKey val gameName: String,
    val highScore: Int = 0,
    val lastPlayedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val wallpaperId: String = "aura_glow",
    val isDarkMode: Boolean = true,
    val isLockScreenEnabled: Boolean = true,
    val lockPin: String = "1234",
    val screenBrightness: Float = 0.85f,
    val mediaVolume: Float = 0.8f,
    val sPenSoundEnabled: Boolean = true,
    val edgePanelEnabled: Boolean = true
)
