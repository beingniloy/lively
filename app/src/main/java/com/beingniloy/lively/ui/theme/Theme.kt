package com.beingniloy.lively.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBAC3FF),
    secondary = Color(0xFFC5C5D4),
    tertiary = Color(0xFFDBDBF4),
    background = Color(0xFF131318),
    surface = Color(0xFF131318),
    onPrimary = Color(0xFF1E1E24),
    onSecondary = Color(0xFF1E1E24),
    onTertiary = Color(0xFF1E1E24),
    onBackground = Color(0xFFE1E0F9),
    onSurface = Color(0xFFE1E0F9)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF24389C),
    secondary = Color(0xFF5E5F74),
    tertiary = Color(0xFF5E5F74),
    background = Color(0xFFFBF8FE),
    surface = Color(0xFFFBF8FE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1B1B20),
    onSurface = Color(0xFF1B1B20)
)

@Composable
fun LivelyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    val appColors = currentAppColors(darkTheme)

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
