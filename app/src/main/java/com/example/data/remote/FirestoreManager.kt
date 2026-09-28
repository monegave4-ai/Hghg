package com.example.data.remote

import android.util.Log
import com.example.data.local.entities.StoreAppEntity
import com.example.data.local.entities.WhatsAppMessageEntity
import com.example.data.local.entities.WhatsAppUserEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreManager {

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("FirestoreManager", "Firestore not initialized or google-services missing: ${e.message}")
            null
        }
    }

    // WhatsApp Cloud Messaging
    fun observeMessagesForUser(username: String): Flow<List<WhatsAppMessageEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val cleanUsername = if (username.startsWith("@")) username else "@$username"

        val registration: ListenerRegistration = db.collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreManager", "Error listening to messages: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val toUser = doc.getString("conversationUsername") ?: ""
                        val fromUser = doc.getString("senderUsername") ?: ""
                        if (toUser.equals(cleanUsername, ignoreCase = true) || fromUser.equals(cleanUsername, ignoreCase = true)) {
                            WhatsAppMessageEntity(
                                id = doc.getLong("localId") ?: doc.hashCode().toLong(),
                                conversationUsername = toUser,
                                senderUsername = fromUser,
                                text = doc.getString("text") ?: "",
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                isFromMe = fromUser.equals(cleanUsername, ignoreCase = true),
                                isRead = doc.getBoolean("isRead") ?: true,
                                mediaType = doc.getString("mediaType") ?: "TEXT",
                                mediaUri = doc.getString("mediaUri") ?: ""
                            )
                        } else null
                    }
                    trySend(list)
                }
            }

        awaitClose { registration.remove() }
    }

    suspend fun sendCloudMessage(message: WhatsAppMessageEntity) {
        val db = firestore ?: return
        try {
            val data = hashMapOf(
                "conversationUsername" to message.conversationUsername,
                "senderUsername" to message.senderUsername,
                "text" to message.text,
                "timestamp" to message.timestamp,
                "mediaType" to message.mediaType,
                "mediaUri" to message.mediaUri,
                "isRead" to message.isRead
            )
            db.collection("messages").add(data).await()
        } catch (e: Exception) {
            Log.w("FirestoreManager", "Failed to write cloud message: ${e.message}")
        }
    }

    // Register Cloud User
    suspend fun registerCloudUser(user: WhatsAppUserEntity) {
        val db = firestore ?: return
        try {
            val data = hashMapOf(
                "username" to user.username,
                "displayName" to user.displayName,
                "statusBio" to user.statusBio,
                "avatarColorHex" to user.avatarColorHex,
                "isOnline" to user.isOnline,
                "lastSeen" to System.currentTimeMillis()
            )
            db.collection("users").document(user.username).set(data).await()
        } catch (e: Exception) {
            Log.w("FirestoreManager", "Failed to register user: ${e.message}")
        }
    }

    fun observeCloudUsers(): Flow<List<WhatsAppUserEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = db.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            if (snapshot != null) {
                val users = snapshot.documents.mapNotNull { doc ->
                    val uname = doc.getString("username") ?: doc.id
                    WhatsAppUserEntity(
                        username = uname,
                        displayName = doc.getString("displayName") ?: uname,
                        statusBio = doc.getString("statusBio") ?: "متاح على واتساب نوت 10",
                        avatarColorHex = doc.getString("avatarColorHex") ?: "#25D366",
                        isOnline = doc.getBoolean("isOnline") ?: true,
                        lastSeenFormatted = "متصل الآن",
                        isSelf = false
                    )
                }
                trySend(users)
            }
        }
        awaitClose { registration.remove() }
    }

    // Global Community App Store
    suspend fun publishGlobalStoreApp(app: StoreAppEntity) {
        val db = firestore ?: return
        try {
            val data = hashMapOf(
                "packageName" to app.packageName,
                "appName" to app.appName,
                "developerName" to app.developerName,
                "iconBgColor" to app.iconBgColor,
                "iconSymbol" to app.iconSymbol,
                "category" to app.category,
                "description" to app.description,
                "version" to app.version,
                "downloadsCount" to app.downloadsCount,
                "rating" to app.rating.toDouble(),
                "sizeFormatted" to app.sizeFormatted,
                "isCommunityPublished" to true,
                "createdAt" to app.createdAt
            )
            db.collection("store_apps").document(app.packageName).set(data).await()
        } catch (e: Exception) {
            Log.w("FirestoreManager", "Failed to publish app to firestore: ${e.message}")
        }
    }

    fun observeGlobalStoreApps(): Flow<List<StoreAppEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = db.collection("store_apps")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val apps = snapshot.documents.mapNotNull { doc ->
                        val pkg = doc.getString("packageName") ?: doc.id
                        StoreAppEntity(
                            packageName = pkg,
                            appName = doc.getString("appName") ?: "تطبيق مجتمعي",
                            developerName = doc.getString("developerName") ?: "مطور",
                            iconBgColor = doc.getString("iconBgColor") ?: "#0072DE",
                            iconSymbol = doc.getString("iconSymbol") ?: "apps",
                            category = doc.getString("category") ?: "أدوات",
                            description = doc.getString("description") ?: "",
                            version = doc.getString("version") ?: "1.0.0",
                            downloadsCount = (doc.getLong("downloadsCount") ?: 1L).toInt(),
                            rating = (doc.getDouble("rating") ?: 4.8).toFloat(),
                            sizeFormatted = doc.getString("sizeFormatted") ?: "15 MB",
                            isInstalled = false,
                            isCommunityPublished = true,
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                    }
                    trySend(apps)
                }
            }
        awaitClose { registration.remove() }
    }
}
