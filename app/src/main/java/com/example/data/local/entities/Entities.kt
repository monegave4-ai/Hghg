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
    val isFingerprintEnabled: Boolean = true,
    val isFaceUnlockEnabled: Boolean = true,
    val screenBrightness: Float = 0.85f,
    val mediaVolume: Float = 0.8f,
    val sPenSoundEnabled: Boolean = true,
    val edgePanelEnabled: Boolean = true,
    val currentWhatsAppUsername: String = "@galaxy_user"
)

@Entity(tableName = "store_apps")
data class StoreAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val developerName: String,
    val iconBgColor: String = "#0072DE",
    val iconSymbol: String = "apps",
    val category: String = "أدوات", // أدوات, ألعاب, تواصل, إنتاجية, وسائط
    val description: String,
    val version: String = "1.0.0",
    val downloadsCount: Int = 120,
    val rating: Float = 4.8f,
    val sizeFormatted: String = "14 MB",
    val isInstalled: Boolean = false,
    val isCommunityPublished: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "gallery_photos")
data class GalleryPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val uriOrResId: String,
    val isCameraCaptured: Boolean = true,
    val filterApplied: String = "NONE",
    val drawingStrokesJson: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedTimestamp: Long = 0L
)

@Entity(tableName = "whatsapp_users")
data class WhatsAppUserEntity(
    @PrimaryKey val username: String, // e.g. @samsung_dev
    val displayName: String,
    val statusBio: String = "متاح على واتساب نوت 10",
    val avatarColorHex: String = "#25D366",
    val isOnline: Boolean = true,
    val lastSeenFormatted: String = "متصل الآن",
    val isSelf: Boolean = false
)

@Entity(tableName = "whatsapp_messages")
data class WhatsAppMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationUsername: String,
    val senderUsername: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromMe: Boolean = true,
    val isRead: Boolean = true,
    val mediaType: String = "TEXT", // TEXT, IMAGE, VOICE, APK
    val mediaUri: String = ""
)
