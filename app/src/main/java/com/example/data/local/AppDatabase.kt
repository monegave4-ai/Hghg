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
        AppSettingsEntity::class,
        StoreAppEntity::class,
        GalleryPhotoEntity::class,
        WhatsAppUserEntity::class,
        WhatsAppMessageEntity::class
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
    abstract fun storeAppDao(): StoreAppDao
    abstract fun galleryPhotoDao(): GalleryPhotoDao
    abstract fun whatsAppDao(): WhatsAppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "galaxy_note10_phone.db"
                ).fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
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
            val storeAppDao = db.storeAppDao()
            val galleryPhotoDao = db.galleryPhotoDao()
            val whatsAppDao = db.whatsAppDao()

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
                    text = "مرحباً بك في جهاز Samsung Galaxy Note 10! استمتع بمزايا متجر التطبيقات الجديد، محاكي واتساب، وحماية البصمة وقفل الوجه.",
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
                    title = "قائمة المهام والتطبيقات 📋",
                    content = "1. تثبيت تطبيق واتساب من Galaxy Store\n2. نشر تطبيق جديد بصيغة APK\n3. تجربة قفل الشاشة بالبصمة والوجه\n4. التقاط صور بالكاميرا وحذفها لسلة المحذوفات",
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
                    lockPin = "1234",
                    isFingerprintEnabled = true,
                    isFaceUnlockEnabled = true,
                    screenBrightness = 0.9f,
                    mediaVolume = 0.85f,
                    currentWhatsAppUsername = "@galaxy_user"
                )
            )

            // Initial Store Apps
            val defaultStoreApps = listOf(
                StoreAppEntity(
                    packageName = "com.samsung.galaxy.store",
                    appName = "Galaxy Store",
                    developerName = "Samsung Electronics Co., Ltd.",
                    iconBgColor = "#E91E63",
                    iconSymbol = "store",
                    category = "أدوات",
                    description = "المتجر الرسمي لتطبيقات وألعاب وثيمات هواتف جالاكسي ونشر ملفات APK لمجتمع المستخدمين.",
                    version = "5.3.0",
                    downloadsCount = 10000,
                    rating = 4.9f,
                    sizeFormatted = "28 MB",
                    isInstalled = true,
                    isCommunityPublished = false
                ),
                StoreAppEntity(
                    packageName = "com.whatsapp.note10",
                    appName = "WhatsApp Messenger",
                    developerName = "Meta / Note10 Comms",
                    iconBgColor = "#25D366",
                    iconSymbol = "whatsapp",
                    category = "تواصل",
                    description = "تطبيق المراسلة الفورية بمجرد اختيار يوزر نيم. يدعم المحادثات الحقيقية ومشاركة الصور والصوتيات وحزم التطبيقات.",
                    version = "2.24.12",
                    downloadsCount = 8500,
                    rating = 4.8f,
                    sizeFormatted = "45 MB",
                    isInstalled = true,
                    isCommunityPublished = false
                ),
                StoreAppEntity(
                    packageName = "com.samsung.photoeditor.spen",
                    appName = "Photo Editor Pro",
                    developerName = "Galaxy Labs Studio",
                    iconBgColor = "#FF5722",
                    iconSymbol = "photo_editor",
                    category = "وسائط",
                    description = "محرر صور احترافي متكامل مع دعم فلاتر الذكاء الاصطناعي والكتابة والتعديل بواسطة قلم S-Pen.",
                    version = "3.1.0",
                    downloadsCount = 4200,
                    rating = 4.7f,
                    sizeFormatted = "22 MB",
                    isInstalled = true,
                    isCommunityPublished = false
                ),
                StoreAppEntity(
                    packageName = "com.arcade.brickbreaker",
                    appName = "Brick Breaker (كسار الطوب)",
                    developerName = "Retro Gaming Arcade",
                    iconBgColor = "#9C27B0",
                    iconSymbol = "brick_breaker",
                    category = "ألعاب",
                    description = "لعبة أركيد الكلاسيكية لتدمير الطوب بالكرة والمضرب مع مؤثرات بصرية وتأثيرات قوة ممتعة.",
                    version = "1.4.2",
                    downloadsCount = 3100,
                    rating = 4.6f,
                    sizeFormatted = "18 MB",
                    isInstalled = true,
                    isCommunityPublished = false
                ),
                StoreAppEntity(
                    packageName = "com.samsung.devicecare",
                    appName = "Device Care (العناية بالجهاز)",
                    developerName = "Samsung System Team",
                    iconBgColor = "#0072DE",
                    iconSymbol = "device_care",
                    category = "إنتاجية",
                    description = "تحسين أداء البطارية والذاكرة العشوائية وتفريغ المساحة وحماية الهاتف بضغطة زر واحدة.",
                    version = "4.0.5",
                    downloadsCount = 7400,
                    rating = 4.9f,
                    sizeFormatted = "12 MB",
                    isInstalled = true,
                    isCommunityPublished = false
                ),
                StoreAppEntity(
                    packageName = "com.community.flappynote",
                    appName = "Flappy S-Pen 🐦",
                    developerName = "المطور: أحمد التقني",
                    iconBgColor = "#FFC107",
                    iconSymbol = "games",
                    category = "ألعاب",
                    description = "تطبيق مجتمعي منشور عبر APK: تحدي الطيران بالريشة وتجاوز العوائق.",
                    version = "1.0.0",
                    downloadsCount = 980,
                    rating = 4.5f,
                    sizeFormatted = "9 MB",
                    isInstalled = false,
                    isCommunityPublished = true
                )
            )
            storeAppDao.insertAllApps(defaultStoreApps)

            // Initial WhatsApp Users
            val initialWhatsAppUsers = listOf(
                WhatsAppUserEntity(
                    username = "@samsung_ai",
                    displayName = "مساعد سامسونج الذكي 🤖",
                    statusBio = "جاهز لمساعدتك في أي استفسار حول نوت 10",
                    avatarColorHex = "#0072DE",
                    isOnline = true,
                    lastSeenFormatted = "متصل الآن"
                ),
                WhatsAppUserEntity(
                    username = "@omar_dev",
                    displayName = "عمر (مطور أندرويد) 💻",
                    statusBio = "أكتب كود بلغة Kotlin و Jetpack Compose",
                    avatarColorHex = "#673AB7",
                    isOnline = true,
                    lastSeenFormatted = "متصل الآن"
                ),
                WhatsAppUserEntity(
                    username = "@sara_designer",
                    displayName = "سارة (مصممة UI/UX) 🎨",
                    statusBio = "تصاميم One UI 6 وألوان Aura Glow",
                    avatarColorHex = "#E91E63",
                    isOnline = false,
                    lastSeenFormatted = "آخر ظهور اليوم 10:45 ص"
                ),
                WhatsAppUserEntity(
                    username = "@khalid_gamer",
                    displayName = "خالد (عاشق الألعاب) 🎮",
                    statusBio = "تحدي لعبة الدودة وكسار الطوب!",
                    avatarColorHex = "#4CAF50",
                    isOnline = true,
                    lastSeenFormatted = "متصل الآن"
                )
            )
            whatsAppDao.insertAllUsers(initialWhatsAppUsers)

            // Seed initial WhatsApp messages
            val initialWaMessages = listOf(
                WhatsAppMessageEntity(
                    conversationUsername = "@samsung_ai",
                    senderUsername = "@samsung_ai",
                    text = "أهلاً بك في محاكي واتساب على هاتفك جالاكسي نوت 10! يمكنك إضافة جهات اتصال باليوزرنيم ومراسلتهم أو مشاركة ملفات APK وتطبيقات المتجر.",
                    timestamp = now - 1800000,
                    isFromMe = false
                ),
                WhatsAppMessageEntity(
                    conversationUsername = "@omar_dev",
                    senderUsername = "@omar_dev",
                    text = "مرحباً يا بطل! متجر التطبيقات الآن جاهز وتستطيع نشر أي تطبيق APK ليراه الجميع.",
                    timestamp = now - 900000,
                    isFromMe = false
                )
            )
            initialWaMessages.forEach { whatsAppDao.insertMessage(it) }

            // Initial Gallery Photos
            val initialPhotos = listOf(
                GalleryPhotoEntity(
                    title = "Galaxy Note 10 Aura Glow",
                    uriOrResId = "note10_wallpaper",
                    isCameraCaptured = false,
                    timestamp = now - 86400000 * 2
                ),
                GalleryPhotoEntity(
                    title = "غروب الشمس بدقة عالية 4K 🌅",
                    uriOrResId = "sunset_captured",
                    isCameraCaptured = true,
                    timestamp = now - 3600000 * 5
                ),
                GalleryPhotoEntity(
                    title = "رسمة بالقلم S-Pen 🎨",
                    uriOrResId = "spen_drawing",
                    isCameraCaptured = false,
                    timestamp = now - 3600000 * 2
                )
            )
            galleryPhotoDao.insertAllPhotos(initialPhotos)
        }
    }
}
