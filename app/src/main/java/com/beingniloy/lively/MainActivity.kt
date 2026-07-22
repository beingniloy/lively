package com.beingniloy.lively

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beingniloy.lively.ui.navigation.AppNavigation
import com.beingniloy.lively.ui.theme.LivelyTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferencesViewModel: PreferencesViewModel = viewModel()
            val wallpaperViewModel: WallpaperViewModel = viewModel()
            
            val isDarkTheme by preferencesViewModel.isDarkTheme.collectAsStateWithLifecycle()
            val useDynamicColor by preferencesViewModel.useDynamicColor.collectAsStateWithLifecycle()

            LivelyTheme(
                darkTheme = isDarkTheme,
                dynamicColor = useDynamicColor
            ) {
                AppNavigation(
                    wallpaperViewModel = wallpaperViewModel,
                    preferencesViewModel = preferencesViewModel
                )
            }
        }
    }
}
