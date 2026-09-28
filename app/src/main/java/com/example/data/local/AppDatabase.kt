package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ContactEntity::class,
        MessageEntity::class,
        NoteEntity::class,
        RecordingEntity::class,
        GameScoreEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun messageDao(): MessageDao
    abstract fun noteDao(): NoteDao
    abstract fun recordingDao(): RecordingDao
    abstract fun gameScoreDao(): GameScoreDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "galaxy_note10_phone.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            populateInitialData(getDatabase(context))
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val contactDao = db.contactDao()
            val messageDao = db.messageDao()
            val noteDao = db.noteDao()
            val settingsDao = db.settingsDao()

            val initialContacts = listOf(
                ContactEntity(
                    id = 1,
                    name = "خدمة عملاء سامسونج",
                    phoneNumber = "8002474357",
                    email = "support@samsung.com",
                    avatarColorHex = "#0072DE",
                    isFavorite = true
                ),
                ContactEntity(
                    id = 2,
                    name = "أمي الغالية ❤️",
                    phoneNumber = "0551234567",
                    email = "mom@family.net",
                    avatarColorHex = "#E91E63",
                    isFavorite = true
                ),
                ContactEntity(
                    id = 3,
                    name = "م. أحمد (العمل)",
                    phoneNumber = "0509876543",
                    email = "ahmed.dev@company.com",
                    avatarColorHex = "#4CAF50",
                    isFavorite = false
                ),
                ContactEntity(
                    id = 4,
                    name = "صديقي خالد",
                    phoneNumber = "0563322114",
                    email = "khaled@mail.com",
                    avatarColorHex = "#FF9800",
                    isFavorite = false
                )
            )
            contactDao.insertAllContacts(initialContacts)

            val now = System.currentTimeMillis()
            val initialMessages = listOf(
                MessageEntity(
                    contactPhoneNumber = "8002474357",
                    senderName = "خدمة عملاء سامسونج",
                    text = "مرحباً بك في جهاز Samsung Galaxy Note 10! استمتع بمزايا قلم S-Pen والشاشة السينمائية وكاميرا الاحترافية.",
                    timestamp = now - 3600000 * 2,
                    isFromMe = false
                ),
                MessageEntity(
                    contactPhoneNumber = "0551234567",
                    senderName = "أمي الغالية ❤️",
                    text = "السلام عليكم يا بني، طمني عنك لما تفضى اتصل بي.",
                    timestamp = now - 3600000 * 1,
                    isFromMe = false
                ),
                MessageEntity(
                    contactPhoneNumber = "0551234567",
                    senderName = "أمي الغالية ❤️",
                    text = "وعليكم السلام يا أمي، أبشري سأتصل بكِ بعد قليل.",
                    timestamp = now - 1800000,
                    isFromMe = true
                )
            )
            messageDao.insertAllMessages(initialMessages)

            val initialNotes = listOf(
                NoteEntity(
                    title = "ملاحظة بقلم S-Pen ✍️",
                    content = "قلم S-Pen في جالكسي نوت 10 يدعم الرسم بدقة فائقة والملاحظات السريعة حتى والشاشة مغلقة!",
                    backgroundColorHex = "#FFF9C4",
                    timestamp = now - 7200000,
                    isPinned = true
                ),
                NoteEntity(
                    title = "قائمة المهام اليومية 📋",
                    content = "1. تجربة لعبة الدودة الجديدة\n2. تسجيل ملاحظة صوتية\n3. التقاط صور بالكاميرا وحفظها في المعرض\n4. تصفح الإنترنت",
                    backgroundColorHex = "#E1F5FE",
                    timestamp = now - 3600000,
                    isPinned = false
                )
            )
            initialNotes.forEach { noteDao.insertNote(it) }

            settingsDao.insertOrUpdateSettings(
                AppSettingsEntity(
                    wallpaperId = "aura_glow",
                    isDarkMode = true,
                    isLockScreenEnabled = false,
                    screenBrightness = 0.9f,
                    mediaVolume = 0.85f
                )
            )
        }
    }
}
