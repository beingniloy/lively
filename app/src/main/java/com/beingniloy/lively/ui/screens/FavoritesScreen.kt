package com.beingniloy.lively.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beingniloy.lively.PreferencesViewModel
import com.beingniloy.lively.WallpaperViewModel
import com.beingniloy.lively.ui.components.LibraryVideoCard
import com.beingniloy.lively.ui.theme.LocalAppColors
import com.beingniloy.lively.ui.theme.NunitoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    wallpaperViewModel: WallpaperViewModel,
    preferencesViewModel: PreferencesViewModel,
    onNavigateToPreview: () -> Unit
) {
    val colors = LocalAppColors.current
    val favorites by wallpaperViewModel.favorites.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Favorites",
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
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceContainerLow),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(60.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "No Favorites Yet",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (colors.isDark) Color.White else Color.Black
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the heart icon on any wallpaper in the Library to save it here.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = NunitoFontFamily,
                            color = colors.textSubtle
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                val chunked = favorites.chunked(2)
                items(chunked) { rowList ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (item in rowList) {
                            LibraryVideoCard(
                                item = item,
                                colors = colors,
                                wallpaperViewModel = wallpaperViewModel,
                                onPreview = {
                                    wallpaperViewModel.applyRecentWallpaper(item)
                                    onNavigateToPreview()
                                },
                                onAddToCollection = {
                                    // Maybe add to collection from here too?
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowList.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
