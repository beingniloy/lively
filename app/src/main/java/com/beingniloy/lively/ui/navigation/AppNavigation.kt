package com.beingniloy.lively.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beingniloy.lively.PreferencesViewModel
import com.beingniloy.lively.WallpaperViewModel
import com.beingniloy.lively.ui.screens.*
import com.beingniloy.lively.ui.theme.LocalAppColors
import com.beingniloy.lively.ui.theme.NunitoFontFamily

@Composable
fun AppNavigation(
    wallpaperViewModel: WallpaperViewModel,
    preferencesViewModel: PreferencesViewModel
) {
    var currentScreen by remember { mutableStateOf("splash") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (currentScreen) {
            "splash" -> {
                SplashScreen(onNavigateToHome = { currentScreen = "main" })
            }
            "main" -> {
                MainAppContainer(
                    wallpaperViewModel = wallpaperViewModel,
                    preferencesViewModel = preferencesViewModel,
                    onNavigateToPreview = { currentScreen = "preview" }
                )
            }
            "preview" -> {
                PreviewScreen(
                    wallpaperViewModel = wallpaperViewModel,
                    onNavigateBack = { currentScreen = "main" }
                )
            }
        }
    }
}

@Composable
fun MainAppContainer(
    wallpaperViewModel: WallpaperViewModel,
    preferencesViewModel: PreferencesViewModel,
    onNavigateToPreview: () -> Unit
) {
    val colors = LocalAppColors.current
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = colors.bg,
                contentColor = colors.accent,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0, 0, 0, 0),
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                listOf(
                    Triple(0, "Home", Icons.Rounded.Home),
                    Triple(1, "Library", Icons.Rounded.VideoLibrary),
                    Triple(2, "Recent", Icons.Rounded.History),
                    Triple(3, "Favorites", Icons.Rounded.Favorite),
                    Triple(4, "Settings", Icons.Rounded.Settings)
                ).forEach { (index, label, icon) ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = NunitoFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colors.accent,
                            selectedTextColor = colors.accent,
                            unselectedIconColor = colors.textSubtle,
                            unselectedTextColor = colors.textSubtle,
                            indicatorColor = colors.accentPurple
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.bg)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "MainContentTransition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> HomeScreen(
                        onNavigateToPreview = onNavigateToPreview,
                        preferencesViewModel = preferencesViewModel,
                        wallpaperViewModel = wallpaperViewModel
                    )
                    1 -> LibraryScreen(
                        wallpaperViewModel = wallpaperViewModel,
                        preferencesViewModel = preferencesViewModel,
                        onNavigateToPreview = onNavigateToPreview,
                        onNavigateToHome = { selectedTab = 0 }
                    )
                    2 -> RecentScreen(
                        wallpaperViewModel = wallpaperViewModel,
                        preferencesViewModel = preferencesViewModel,
                        onNavigateToPreview = onNavigateToPreview
                    )
                    3 -> FavoritesScreen(
                        wallpaperViewModel = wallpaperViewModel,
                        preferencesViewModel = preferencesViewModel,
                        onNavigateToPreview = onNavigateToPreview
                    )
                    4 -> SettingsScreen(
                        wallpaperViewModel = wallpaperViewModel,
                        preferencesViewModel = preferencesViewModel
                    )
                }
            }
        }
    }
}
