package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.local.entities.StoreAppEntity
import com.example.data.local.entities.WhatsAppMessageEntity
import com.example.data.local.entities.WhatsAppUserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class SupabaseConnectionStatus {
    NOT_CONFIGURED,
    CONNECTING,
    CONNECTED,
    ERROR
}

data class SupabaseConfig(
    val url: String = "",
    val anonKey: String = "",
    val isConfigured: Boolean = false
)

class SupabaseManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("supabase_config", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun getConfig(): SupabaseConfig {
        val url = prefs.getString("supabase_url", "")?.trim().orEmpty()
        val key = prefs.getString("supabase_anon_key", "")?.trim().orEmpty()
        val cleanUrl = url.trimEnd('/')
        return SupabaseConfig(
            url = cleanUrl,
            anonKey = key,
            isConfigured = cleanUrl.isNotBlank() && key.isNotBlank()
        )
    }

    fun saveConfig(url: String, key: String) {
        val cleanUrl = url.trim().trimEnd('/')
        val cleanKey = key.trim()
        prefs.edit()
            .putString("supabase_url", cleanUrl)
            .putString("supabase_anon_key", cleanKey)
            .apply()
    }

    fun clearConfig() {
        prefs.edit().clear().apply()
    }

    private fun buildRequest(endpoint: String, method: String = "GET", bodyJson: String? = null): Request? {
        val config = getConfig()
        if (!config.isConfigured) return null

        val fullUrl = "${config.url}/rest/v1/$endpoint"
        val builder = Request.Builder()
            .url(fullUrl)
            .addHeader("apikey", config.anonKey)
            .addHeader("Authorization", "Bearer ${config.anonKey}")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=representation")

        if (method == "POST" && bodyJson != null) {
            builder.post(bodyJson.toRequestBody(jsonMediaType))
        } else if (method == "PATCH" && bodyJson != null) {
            builder.patch(bodyJson.toRequestBody(jsonMediaType))
        } else if (method == "PUT" && bodyJson != null) {
            builder.put(bodyJson.toRequestBody(jsonMediaType))
        } else if (method == "DELETE") {
            builder.delete()
        }

        return builder.build()
    }

    suspend fun testConnection(): Pair<SupabaseConnectionStatus, String> = withContext(Dispatchers.IO) {
        val config = getConfig()
        if (!config.isConfigured) {
            return@withContext Pair(SupabaseConnectionStatus.NOT_CONFIGURED, "الرابط أو المفتاح غير مدخل.")
        }

        try {
            val request = Request.Builder()
                .url("${config.url}/rest/v1/")
                .addHeader("apikey", config.anonKey)
                .addHeader("Authorization", "Bearer ${config.anonKey}")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful || response.code == 200 || response.code == 404 || response.code == 401) {
                if (response.code == 401) {
                    return@withContext Pair(SupabaseConnectionStatus.ERROR, "مفتاح Supabase غير صالح (401 Unauthorized)")
                }
                return@withContext Pair(SupabaseConnectionStatus.CONNECTED, "تم الاتصال بسوباباس بنجاح 🟢")
            } else {
                return@withContext Pair(SupabaseConnectionStatus.ERROR, "خطأ بالاستجابة: رمز ${response.code}")
            }
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Connection test failed", e)
            return@withContext Pair(SupabaseConnectionStatus.ERROR, "تعذر الوصول للسيرفر: ${e.localizedMessage ?: "تأكد من الرابط والاتصال"}")
        }
    }

    // ==========================================
    // 💬 WhatsApp Messages Sync
    // ==========================================

    suspend fun sendCloudMessage(message: WhatsAppMessageEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("conversation_username", message.conversationUsername)
                put("sender_username", message.senderUsername)
                put("text", message.text)
                put("timestamp", message.timestamp)
                put("is_from_me", message.isFromMe)
                put("is_read", message.isRead)
                put("media_type", message.mediaType)
                put("media_uri", message.mediaUri)
            }

            val request = buildRequest("messages", "POST", json.toString()) ?: return@withContext false
            val response = client.newCall(request).execute()
            val success = response.isSuccessful || response.code == 201
            response.close()
            success
        } catch (e: Exception) {
            Log.w("SupabaseManager", "Failed to send message: ${e.message}")
            false
        }
    }

    suspend fun fetchMessagesForUser(username: String): List<WhatsAppMessageEntity> = withContext(Dispatchers.IO) {
        val config = getConfig()
        if (!config.isConfigured) return@withContext emptyList()

        try {
            val cleanUsername = if (username.startsWith("@")) username else "@$username"
            // Fetch messages where sender or conversation is this user or all
            val endpoint = "messages?select=*&order=timestamp.asc"
            val request = buildRequest(endpoint) ?: return@withContext emptyList()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                return@withContext emptyList()
            }

            val body = response.body?.string().orEmpty()
            response.close()

            val jsonArray = JSONArray(body)
            val list = mutableListOf<WhatsAppMessageEntity>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val convUser = obj.optString("conversation_username", "")
                val senderUser = obj.optString("sender_username", "")

                // If user is part of the conversation
                val isRelevant = convUser.equals(cleanUsername, ignoreCase = true) ||
                        senderUser.equals(cleanUsername, ignoreCase = true) ||
                        convUser.isBlank()

                if (isRelevant) {
                    list.add(
                        WhatsAppMessageEntity(
                            id = obj.optLong("id", obj.optLong("timestamp", System.currentTimeMillis())),
                            conversationUsername = convUser,
                            senderUsername = senderUser,
                            text = obj.optString("text", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            isFromMe = senderUser.equals(cleanUsername, ignoreCase = true),
                            isRead = obj.optBoolean("is_read", true),
                            mediaType = obj.optString("media_type", "TEXT"),
                            mediaUri = obj.optString("media_uri", "")
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            Log.w("SupabaseManager", "Error fetching messages: ${e.message}")
            emptyList()
        }
    }

    fun pollMessagesFlow(username: String, intervalMs: Long = 4000L): Flow<List<WhatsAppMessageEntity>> = flow {
        while (true) {
            val list = fetchMessagesForUser(username)
            if (list.isNotEmpty()) {
                emit(list)
            }
            delay(intervalMs)
        }
    }.flowOn(Dispatchers.IO)

    // ==========================================
    // 👤 WhatsApp Users Sync
    // ==========================================

    suspend fun registerUser(user: WhatsAppUserEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("username", user.username)
                put("display_name", user.displayName)
                put("status_bio", user.statusBio)
                put("avatar_color_hex", user.avatarColorHex)
                put("is_online", user.isOnline)
                put("last_seen_formatted", user.lastSeenFormatted)
            }

            // Using upsert with Prefer: resolution=merge-duplicates
            val config = getConfig()
            if (!config.isConfigured) return@withContext false

            val request = Request.Builder()
                .url("${config.url}/rest/v1/whatsapp_users")
                .addHeader("apikey", config.anonKey)
                .addHeader("Authorization", "Bearer ${config.anonKey}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val success = response.isSuccessful || response.code == 201 || response.code == 200
            response.close()
            success
        } catch (e: Exception) {
            Log.w("SupabaseManager", "Failed to register user: ${e.message}")
            false
        }
    }

    suspend fun fetchUsers(): List<WhatsAppUserEntity> = withContext(Dispatchers.IO) {
        try {
            val request = buildRequest("whatsapp_users?select=*") ?: return@withContext emptyList()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                return@withContext emptyList()
            }

            val body = response.body?.string().orEmpty()
            response.close()

            val jsonArray = JSONArray(body)
            val list = mutableListOf<WhatsAppUserEntity>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val uname = obj.optString("username", "")
                if (uname.isNotBlank()) {
                    list.add(
                        WhatsAppUserEntity(
                            username = uname,
                            displayName = obj.optString("display_name", uname),
                            statusBio = obj.optString("status_bio", "متاح على واتساب نوت 10"),
                            avatarColorHex = obj.optString("avatar_color_hex", "#25D366"),
                            isOnline = obj.optBoolean("is_online", true),
                            lastSeenFormatted = obj.optString("last_seen_formatted", "متصل الآن"),
                            isSelf = false
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            Log.w("SupabaseManager", "Error fetching users: ${e.message}")
            emptyList()
        }
    }

    fun pollUsersFlow(intervalMs: Long = 10000L): Flow<List<WhatsAppUserEntity>> = flow {
        while (true) {
            val list = fetchUsers()
            if (list.isNotEmpty()) {
                emit(list)
            }
            delay(intervalMs)
        }
    }.flowOn(Dispatchers.IO)

    // ==========================================
    // 🛍️ Galaxy Store Apps Sync
    // ==========================================

    suspend fun publishStoreApp(app: StoreAppEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("package_name", app.packageName)
                put("app_name", app.appName)
                put("developer_name", app.developerName)
                put("icon_bg_color", app.iconBgColor)
                put("icon_symbol", app.iconSymbol)
                put("category", app.category)
                put("description", app.description)
                put("version", app.version)
                put("downloads_count", app.downloadsCount)
                put("rating", app.rating.toDouble())
                put("size_formatted", app.sizeFormatted)
                put("is_installed", false)
                put("is_community_published", true)
                put("created_at", app.createdAt)
            }

            val config = getConfig()
            if (!config.isConfigured) return@withContext false

            val request = Request.Builder()
                .url("${config.url}/rest/v1/store_apps")
                .addHeader("apikey", config.anonKey)
                .addHeader("Authorization", "Bearer ${config.anonKey}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val success = response.isSuccessful || response.code == 201 || response.code == 200
            response.close()
            success
        } catch (e: Exception) {
            Log.w("SupabaseManager", "Failed to publish store app: ${e.message}")
            false
        }
    }

    suspend fun fetchStoreApps(): List<StoreAppEntity> = withContext(Dispatchers.IO) {
        try {
            val request = buildRequest("store_apps?select=*&order=created_at.desc") ?: return@withContext emptyList()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                return@withContext emptyList()
            }

            val body = response.body?.string().orEmpty()
            response.close()

            val jsonArray = JSONArray(body)
            val list = mutableListOf<StoreAppEntity>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val pkg = obj.optString("package_name", "")
                if (pkg.isNotBlank()) {
                    list.add(
                        StoreAppEntity(
                            packageName = pkg,
                            appName = obj.optString("app_name", "تطبيق سوباباس"),
                            developerName = obj.optString("developer_name", "مطور مجتمعي"),
                            iconBgColor = obj.optString("icon_bg_color", "#0072DE"),
                            iconSymbol = obj.optString("icon_symbol", "apps"),
                            category = obj.optString("category", "أدوات"),
                            description = obj.optString("description", ""),
                            version = obj.optString("version", "1.0.0"),
                            downloadsCount = obj.optInt("downloads_count", 1),
                            rating = obj.optDouble("rating", 4.9).toFloat(),
                            sizeFormatted = obj.optString("size_formatted", "15 MB"),
                            isInstalled = false,
                            isCommunityPublished = true,
                            createdAt = obj.optLong("created_at", System.currentTimeMillis())
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            Log.w("SupabaseManager", "Error fetching store apps: ${e.message}")
            emptyList()
        }
    }

    fun pollStoreAppsFlow(intervalMs: Long = 12000L): Flow<List<StoreAppEntity>> = flow {
        while (true) {
            val list = fetchStoreApps()
            if (list.isNotEmpty()) {
                emit(list)
            }
            delay(intervalMs)
        }
    }.flowOn(Dispatchers.IO)

    companion object {
        const val SQL_SCHEMA_SCRIPT = """-- =============================================
-- كود إنشاء جداول Supabase لتطبيق Samsung Galaxy Note 10
-- الصق هذا الكود في Supabase SQL Editor ثم اضغط Run
-- =============================================

-- 1. جدول الرسائل الفورية الحقيقية (WhatsApp Messages)
create table if not exists messages (
  id bigint generated by default as identity primary key,
  conversation_username text,
  sender_username text,
  text text,
  timestamp bigint default (extract(epoch from now()) * 1000)::bigint,
  is_from_me boolean default false,
  is_read boolean default true,
  media_type text default 'TEXT',
  media_uri text default ''
);

-- 2. جدول مستخدمي المحادثات (Users)
create table if not exists whatsapp_users (
  username text primary key,
  display_name text,
  status_bio text default 'متاح على واتساب نوت 10',
  avatar_color_hex text default '#25D366',
  is_online boolean default true,
  last_seen_formatted text default 'متصل الآن'
);

-- 3. جدول متجر التطبيقات المجتمعي (Galaxy Store Apps & APKs)
create table if not exists store_apps (
  package_name text primary key,
  app_name text not null,
  developer_name text default 'مطور مجتمعي',
  icon_bg_color text default '#0072DE',
  icon_symbol text default 'apps',
  category text default 'أدوات',
  description text default '',
  version text default '1.0.0',
  downloads_count int default 1,
  rating real default 5.0,
  size_formatted text default '15 MB',
  is_installed boolean default false,
  is_community_published boolean default true,
  created_at bigint default (extract(epoch from now()) * 1000)::bigint
);

-- تعطيل Row Level Security (RLS) للسماح بالقراءة والكتابة للـ Anon Key
alter table messages disable row level security;
alter table whatsapp_users disable row level security;
alter table store_apps disable row level security;
"""
    }
}
