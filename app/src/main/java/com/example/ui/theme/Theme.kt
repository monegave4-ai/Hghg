package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SamsungBlueLight,
    onPrimary = OneUIDarkBg,
    primaryContainer = SamsungBlueDark,
    onPrimaryContainer = OneUIDarkTextPrimary,
    secondary = AuraCyan,
    onSecondary = OneUIDarkBg,
    background = OneUIDarkBg,
    onBackground = OneUIDarkTextPrimary,
    surface = OneUIDarkSurface,
    onSurface = OneUIDarkTextPrimary,
    surfaceVariant = OneUIDarkCard,
    onSurfaceVariant = OneUIDarkTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = SamsungBlue,
    onPrimary = OneUILightSurface,
    primaryContainer = SamsungBlueLight,
    onPrimaryContainer = OneUILightSurface,
    secondary = AuraViolet,
    onSecondary = OneUILightSurface,
    background = OneUILightBg,
    onBackground = OneUILightTextPrimary,
    surface = OneUILightSurface,
    onSurface = OneUILightTextPrimary,
    surfaceVariant = OneUILightCard,
    onSurfaceVariant = OneUILightTextSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Note10Bezel.toArgb()
            window.navigationBarColor = Note10Bezel.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
