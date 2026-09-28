package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getContactByPhone(phone: String): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllContacts(contacts: List<ContactEntity>)

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE contactPhoneNumber = :phone ORDER BY timestamp ASC")
    fun getMessagesForConversation(phone: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMessages(messages: List<MessageEntity>)

    @Query("DELETE FROM messages WHERE contactPhoneNumber = :phone")
    suspend fun deleteConversation(phone: String)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)
}

@Dao
interface RecordingDao {
    @Query("SELECT * FROM recordings ORDER BY timestamp DESC")
    fun getAllRecordings(): Flow<List<RecordingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: RecordingEntity): Long

    @Delete
    suspend fun deleteRecording(recording: RecordingEntity)
}

@Dao
interface GameScoreDao {
    @Query("SELECT * FROM game_scores WHERE gameName = :gameName LIMIT 1")
    suspend fun getScore(gameName: String): GameScoreEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateScore(score: GameScoreEntity)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: AppSettingsEntity)
}

@Dao
interface StoreAppDao {
    @Query("SELECT * FROM store_apps ORDER BY isCommunityPublished DESC, downloadsCount DESC")
    fun getAllStoreApps(): Flow<List<StoreAppEntity>>

    @Query("SELECT * FROM store_apps WHERE isInstalled = 1")
    fun getInstalledApps(): Flow<List<StoreAppEntity>>

    @Query("SELECT * FROM store_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getAppByPackage(packageName: String): StoreAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateApp(app: StoreAppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllApps(apps: List<StoreAppEntity>)

    @Query("UPDATE store_apps SET isInstalled = :isInstalled WHERE packageName = :packageName")
    suspend fun updateInstallState(packageName: String, isInstalled: Boolean)

    @Delete
    suspend fun deleteApp(app: StoreAppEntity)
}

@Dao
interface GalleryPhotoDao {
    @Query("SELECT * FROM gallery_photos WHERE isDeleted = 0 ORDER BY timestamp DESC")
    fun getActivePhotos(): Flow<List<GalleryPhotoEntity>>

    @Query("SELECT * FROM gallery_photos WHERE isDeleted = 1 ORDER BY deletedTimestamp DESC")
    fun getTrashPhotos(): Flow<List<GalleryPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: GalleryPhotoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPhotos(photos: List<GalleryPhotoEntity>)

    @Query("UPDATE gallery_photos SET isDeleted = 1, deletedTimestamp = :deletedTime WHERE id = :id")
    suspend fun moveToTrash(id: Long, deletedTime: Long = System.currentTimeMillis())

    @Query("UPDATE gallery_photos SET isDeleted = 0, deletedTimestamp = 0 WHERE id = :id")
    suspend fun restoreFromTrash(id: Long)

    @Query("DELETE FROM gallery_photos WHERE id = :id")
    suspend fun permanentlyDeletePhoto(id: Long)

    @Query("DELETE FROM gallery_photos WHERE isDeleted = 1")
    suspend fun emptyTrash()
}

@Dao
interface WhatsAppDao {
    @Query("SELECT * FROM whatsapp_users ORDER BY isOnline DESC, displayName ASC")
    fun getAllUsers(): Flow<List<WhatsAppUserEntity>>

    @Query("SELECT * FROM whatsapp_users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): WhatsAppUserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: WhatsAppUserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllUsers(users: List<WhatsAppUserEntity>)

    @Query("SELECT * FROM whatsapp_messages WHERE conversationUsername = :username ORDER BY timestamp ASC")
    fun getMessagesForUser(username: String): Flow<List<WhatsAppMessageEntity>>

    @Query("SELECT * FROM whatsapp_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<WhatsAppMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: WhatsAppMessageEntity): Long

    @Query("DELETE FROM whatsapp_messages WHERE conversationUsername = :username")
    suspend fun deleteConversation(username: String)
}
