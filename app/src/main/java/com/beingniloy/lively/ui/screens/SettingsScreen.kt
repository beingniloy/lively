package com.beingniloy.lively.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beingniloy.lively.PreferencesViewModel
import com.beingniloy.lively.WallpaperViewModel
import com.beingniloy.lively.ui.theme.LocalAppColors
import com.beingniloy.lively.ui.theme.NunitoFontFamily

sealed interface SettingsDialogType {
    object ClearRecents : SettingsDialogType
    object ClearFavorites : SettingsDialogType
    object ClearStorage : SettingsDialogType
    object ClearCache : SettingsDialogType
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    wallpaperViewModel: WallpaperViewModel,
    preferencesViewModel: PreferencesViewModel
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    val batterySaver by wallpaperViewModel.batterySaver.collectAsStateWithLifecycle()
    val loopPlayback by wallpaperViewModel.loopPlayback.collectAsStateWithLifecycle()
    val isDarkTheme by preferencesViewModel.isDarkTheme.collectAsStateWithLifecycle()
    val useDynamicColor by preferencesViewModel.useDynamicColor.collectAsStateWithLifecycle()
    val cacheSize by wallpaperViewModel.cacheSize.collectAsStateWithLifecycle()
    val storageSize by wallpaperViewModel.storageSize.collectAsStateWithLifecycle()

    var activeDialog by remember { mutableStateOf<SettingsDialogType?>(null) }
    var showBatteryDiagnostics by remember { mutableStateOf(false) }

    if (showBatteryDiagnostics) {
        BatteryDiagnosticsScreen(
            wallpaperViewModel = wallpaperViewModel,
            onNavigateBack = { showBatteryDiagnostics = false }
        )
        return
    }

    LaunchedEffect(Unit) {
        wallpaperViewModel.updateSizes()
    }

    if (activeDialog != null) {
        val titleText = when (activeDialog) {
            SettingsDialogType.ClearRecents -> "Clear Recent Wallpapers?"
            SettingsDialogType.ClearFavorites -> "Clear Favorites?"
            SettingsDialogType.ClearStorage -> "Clear Storage & Reset?"
            SettingsDialogType.ClearCache -> "Clear App Cache?"
            else -> ""
        }
        val descText = when (activeDialog) {
            SettingsDialogType.ClearRecents -> "This will remove local copies of imported wallpapers that are not favorited."
            SettingsDialogType.ClearFavorites -> "This will unfavorite all wallpapers, and remove those not currently set as active or recent."
            SettingsDialogType.ClearStorage -> "WARNING: This will permanently delete all saved wallpapers, collections, and reset the active wallpaper configuration."
            SettingsDialogType.ClearCache -> "This will delete temporary preview files and cache to free up space. (Current: $cacheSize)"
            else -> ""
        }
        val confirmAction = {
            when (activeDialog) {
                SettingsDialogType.ClearRecents -> wallpaperViewModel.clearRecentWallpapers()
                SettingsDialogType.ClearFavorites -> wallpaperViewModel.clearFavorites()
                SettingsDialogType.ClearStorage -> wallpaperViewModel.clearAllStorage()
                SettingsDialogType.ClearCache -> wallpaperViewModel.clearCache()
                else -> {}
            }
            activeDialog = null
        }

        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = NunitoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = if (colors.isDark) Color.White else Color.Black
                    )
                )
            },
            text = {
                Text(
                    text = descText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = NunitoFontFamily,
                        color = colors.textMuted
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = confirmAction,
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.accent)
                ) {
                    Text(
                        text = "Clear",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { activeDialog = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.textSubtle)
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = NunitoFontFamily
                        )
                    )
                }
            },
            containerColor = colors.surfaceContainerLow,
            shape = RoundedCornerShape(28.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (colors.isDark) Color.White else Color.Black,
                            letterSpacing = (-0.5).sp
                        )
                    )
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.bg)
            )
        },
        containerColor = colors.bg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            
            SettingsSection(title = "Appearance") {
                SettingsSwitchRow(
                    icon = Icons.Rounded.DarkMode,
                    title = "Dark Theme",
                    subtitle = "Switch between light and dark UI",
                    checked = isDarkTheme,
                    onCheckedChange = { preferencesViewModel.setDarkTheme(it) }
                )
                SettingsSwitchRow(
                    icon = Icons.Rounded.Palette,
                    title = "Dynamic Colors",
                    subtitle = "Use Material 3 dynamic color scheme",
                    checked = useDynamicColor,
                    onCheckedChange = { preferencesViewModel.setDynamicColor(it) }
                )
            }

            SettingsSection(title = "Performance") {
                SettingsSwitchRow(
                    icon = Icons.Rounded.BatteryChargingFull,
                    title = "Battery Saver",
                    subtitle = "Pause playback when battery is low",
                    checked = batterySaver,
                    onCheckedChange = { wallpaperViewModel.setBatterySaver(it) }
                )
                SettingsSwitchRow(
                    icon = Icons.Rounded.Loop,
                    title = "Continuous Loop",
                    subtitle = "Smoothly repeat video wallpaper",
                    checked = loopPlayback,
                    onCheckedChange = { wallpaperViewModel.setLoopPlayback(it) }
                )
                Divider(
                    color = colors.surfaceContainerHigh,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                SettingsActionRow(
                    icon = Icons.Rounded.QueryStats,
                    title = "Battery Consumption Test",
                    subtitle = "Analyze real-time power drain & efficiency stats",
                    onClick = { showBatteryDiagnostics = true }
                )
            }

            SettingsSection(title = "Storage & Cache") {
                SettingsActionRow(
                    icon = Icons.Rounded.Delete,
                    title = "Clear Cache",
                    subtitle = "Delete temporary visual assets. Cache size: $cacheSize",
                    onClick = { activeDialog = SettingsDialogType.ClearCache }
                )
                SettingsActionRow(
                    icon = Icons.Rounded.History,
                    title = "Clear Recent Wallpapers",
                    subtitle = "Remove local imported wallpaper histories",
                    onClick = { activeDialog = SettingsDialogType.ClearRecents }
                )
                SettingsActionRow(
                    icon = Icons.Rounded.FavoriteBorder,
                    title = "Clear Favorites",
                    subtitle = "Remove all favorited video wallpapers",
                    onClick = { activeDialog = SettingsDialogType.ClearFavorites }
                )
                SettingsActionRow(
                    icon = Icons.Rounded.DeleteForever,
                    title = "Clear App Storage",
                    subtitle = "Fully clean database & wallpapers. Storage: $storageSize",
                    onClick = { activeDialog = SettingsDialogType.ClearStorage }
                )
            }

            SettingsSection(title = "Community & Support") {
                SettingsActionRow(
                    icon = Icons.Rounded.Star,
                    title = "Give a Star ⭐️",
                    subtitle = "Support Lively on GitHub repository",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/beingniloy/Lively"))
                        context.startActivity(intent)
                    }
                )
                SettingsActionRow(
                    icon = Icons.Rounded.Share,
                    title = "Share Lively App",
                    subtitle = "Share Lively with friends & family",
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Lively Wallpaper App")
                            putExtra(Intent.EXTRA_TEXT, "Hey! Check out Lively, an amazing video live wallpaper engine for Android: https://github.com/beingniloy/Lively")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Lively Wallpaper"))
                    }
                )
                SettingsActionRow(
                    icon = Icons.Rounded.BugReport,
                    title = "Report an Issue",
                    subtitle = "Encountered a bug? Report it on GitHub",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/beingniloy/Lively/issues"))
                        context.startActivity(intent)
                    }
                )
            }

            SettingsSection(title = "About") {
                SettingsActionRow(
                    icon = Icons.Rounded.Info,
                    title = "Version",
                    subtitle = "1.0.0 (Production Build)",
                    onClick = { }
                )
                SettingsActionRow(
                    icon = Icons.Rounded.Code,
                    title = "Developer",
                    subtitle = "Built with ❤️ by Niloy",
                    onClick = { 
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/beingniloy"))
                        context.startActivity(intent)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalAppColors.current
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Bold,
                color = colors.accent
            ),
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(colors.surfaceContainerLow)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = NunitoFontFamily, fontWeight = FontWeight.Bold, color = if (colors.isDark) Color.White else Color.Black)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = NunitoFontFamily, color = colors.textSubtle)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.accent,
                checkedTrackColor = colors.accent.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = NunitoFontFamily, fontWeight = FontWeight.Bold, color = if (colors.isDark) Color.White else Color.Black)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = NunitoFontFamily, color = colors.textSubtle)
            )
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = colors.textSubtle)
    }
}
