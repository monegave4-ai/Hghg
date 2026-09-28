package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AppId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Galaxy Note 10", appName)
    }

    @Test
    fun `verify app ids integrity`() {
        val appList = AppId.values()
        assertTrue(appList.contains(AppId.PHONE))
        assertTrue(appList.contains(AppId.MESSAGES))
        assertTrue(appList.contains(AppId.SAMSUNG_NOTES))
        assertTrue(appList.contains(AppId.CAMERA))
        assertTrue(appList.contains(AppId.VOICE_RECORDER))
        assertTrue(appList.contains(AppId.GALLERY))
        assertTrue(appList.contains(AppId.FILE_MANAGER))
        assertTrue(appList.contains(AppId.SNAKE_GAME))
        assertTrue(appList.contains(AppId.CALCULATOR))
        assertTrue(appList.contains(AppId.CLOCK))
        assertTrue(appList.contains(AppId.SETTINGS))
        assertTrue(appList.contains(AppId.BROWSER))
        assertTrue(appList.contains(AppId.MUSIC))
        assertTrue(appList.contains(AppId.WEATHER))
        assertTrue(appList.contains(AppId.GALAXY_STORE))
        assertTrue(appList.contains(AppId.WHATSAPP))
        assertTrue(appList.contains(AppId.PHOTO_EDITOR))
        assertTrue(appList.contains(AppId.BRICK_BREAKER))
        assertTrue(appList.contains(AppId.DEVICE_CARE))
        assertTrue(appList.contains(AppId.TIC_TAC_TOE))
    }

    @Test
    fun `verify supabase manager configuration and sql schema`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val supabaseManager = com.example.data.remote.SupabaseManager(context)
        
        supabaseManager.saveConfig("https://demo12345.supabase.co", "sample_anon_key_abcdef")
        val config = supabaseManager.getConfig()
        assertEquals("https://demo12345.supabase.co", config.url)
        assertEquals("sample_anon_key_abcdef", config.anonKey)
        assertTrue(config.isConfigured)

        assertTrue(com.example.data.remote.SupabaseManager.SQL_SCHEMA_SCRIPT.contains("create table if not exists messages"))
        assertTrue(com.example.data.remote.SupabaseManager.SQL_SCHEMA_SCRIPT.contains("create table if not exists store_apps"))
        assertTrue(com.example.data.remote.SupabaseManager.SQL_SCHEMA_SCRIPT.contains("create table if not exists whatsapp_users"))
    }
}
