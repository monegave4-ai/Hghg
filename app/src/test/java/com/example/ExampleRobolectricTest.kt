package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AppId
import com.example.data.model.SnakeDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    }
}
