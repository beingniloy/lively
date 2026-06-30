package com.beingniloy.lively.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.beingniloy.lively.PreferencesViewModel

// Theme-Aware Dynamic Color System
data class AppColors(
    val bg: Color,
    val cardBg: Color,
    val surfaceContainerLow: Color,
    val surfaceContainerHigh: Color,
    val accent: Color,
    val accentPurple: Color,
    val onSecondaryContainer: Color,
    val border: Color,
    val borderLight: Color,
    val textMuted: Color,
    val textSubtle: Color,
    val isDark: Boolean
)

val LocalAppColors = staticCompositionLocalOf {
    AppColors(
        bg = Color(0xFF131318),
        cardBg = Color(0xFF1E1E24),
        surfaceContainerLow = Color(0xFF1B1B20),
        surfaceContainerHigh = Color(0xFF2E2F34),
        accent = Color(0xFFBAC3FF),
        accentPurple = Color(0xFF444559),
        onSecondaryContainer = Color(0xFFE1E0F9),
        border = Color(0xFF454652),
        borderLight = Color(0x4D454652),
        textMuted = Color(0xFFC5C5D4),
        textSubtle = Color(0xFF8E90A1),
        isDark = true
    )
}

@Composable
fun currentAppColors(isDark: Boolean): AppColors {
    return if (isDark) {
        AppColors(
            bg = Color(0xFF131318),
            cardBg = Color(0xFF1E1E24),
            surfaceContainerLow = Color(0xFF1B1B20),
            surfaceContainerHigh = Color(0xFF2E2F34),
            accent = Color(0xFFBAC3FF),
            accentPurple = Color(0xFF444559),
            onSecondaryContainer = Color(0xFFE1E0F9),
            border = Color(0xFF454652),
            borderLight = Color(0x4D454652),
            textMuted = Color(0xFFC5C5D4),
            textSubtle = Color(0xFF8E90A1),
            isDark = true
        )
    } else {
        AppColors(
            bg = Color(0xFFFBF8FE),
            cardBg = Color(0xFFF0EDF2),
            surfaceContainerLow = Color(0xFFF6F2F8),
            surfaceContainerHigh = Color(0xFFEAE7ED),
            accent = Color(0xFF24389C),
            accentPurple = Color(0xFFDBDBF4),
            onSecondaryContainer = Color(0xFF5E5F74),
            border = Color(0xFFC5C5D4),
            borderLight = Color(0x4DC5C5D4),
            textMuted = Color(0xFF454652),
            textSubtle = Color(0xFF757684),
            isDark = false
        )
    }
}
