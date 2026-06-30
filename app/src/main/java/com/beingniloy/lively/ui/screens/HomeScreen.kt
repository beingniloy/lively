package com.beingniloy.lively.ui.screens

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beingniloy.lively.LivelyWallpaperService
import com.beingniloy.lively.PreferencesViewModel
import com.beingniloy.lively.WallpaperState
import com.beingniloy.lively.WallpaperViewModel
import com.beingniloy.lively.ui.components.VideoPlayerPreview
import com.beingniloy.lively.ui.theme.LocalAppColors
import com.beingniloy.lively.ui.theme.NunitoFontFamily
import com.beingniloy.lively.utils.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPreview: () -> Unit,
    preferencesViewModel: PreferencesViewModel,
    wallpaperViewModel: WallpaperViewModel
) {
    val colors = LocalAppColors.current
    val wallpaperState by wallpaperViewModel.wallpaperState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showHomeScreenSuccessModal by remember { mutableStateOf(false) }
    
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            wallpaperViewModel.selectVideo(uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Lively Wallpaper",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (colors.isDark) Color.White else Color.Black,
                            letterSpacing = (-0.5).sp
                        )
                    )
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.bg
                )
            )
        },
        containerColor = colors.bg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            ) {
                when (wallpaperState) {
                    is WallpaperState.Copying -> {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(420.dp)
                                .border(1.dp, colors.border, RoundedCornerShape(24.dp)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = colors.accent,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "Importing Video...",
                                    color = if (colors.isDark) Color.White else Color.Black,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                    is WallpaperState.Selected -> {
                        val videoFilePath = (wallpaperState as WallpaperState.Selected).filePath
                        val fileName = (wallpaperState as WallpaperState.Selected).fileName
                        val sizeBytes = (wallpaperState as WallpaperState.Selected).sizeBytes
                        val metadata = remember(videoFilePath) { FileUtils.getVideoMetadata(videoFilePath) }
                        var isPlaying by remember { mutableStateOf(true) }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.VideoLibrary,
                                    contentDescription = null,
                                    tint = colors.textSubtle,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Ready to Set",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontFamily = NunitoFontFamily,
                                        color = colors.textSubtle,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(9f / 16f)
                                    .border(1.dp, colors.border, RoundedCornerShape(24.dp))
                                    .clickable { isPlaying = !isPlaying },
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    VideoPlayerPreview(
                                        videoPath = videoFilePath,
                                        fitMode = "center_crop",
                                        scale = 1.0f,
                                        isPlaying = isPlaying,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    if (!isPlaying) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (colors.isDark) Color.Black.copy(alpha = 0.6f)
                                                    else Color.White.copy(alpha = 0.85f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.PlayArrow,
                                                contentDescription = "Play Icon Overlay",
                                                tint = colors.accent,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(16.dp)
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.5f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Pause,
                                                contentDescription = "Playing",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = colors.surfaceContainerLow
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, colors.border, RoundedCornerShape(24.dp)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    verticalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "File Name",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = NunitoFontFamily,
                                                    color = colors.textSubtle,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = fileName,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = NunitoFontFamily,
                                                    color = if (colors.isDark) Color.White else Color.Black,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Resolution",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = NunitoFontFamily,
                                                    color = colors.textSubtle,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = metadata.second,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = NunitoFontFamily,
                                                    color = if (colors.isDark) Color.White else Color.Black,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = colors.border.copy(alpha = 0.5f), thickness = 0.5.dp)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Duration",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = NunitoFontFamily,
                                                    color = colors.textSubtle,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = metadata.first,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = NunitoFontFamily,
                                                    color = if (colors.isDark) Color.White else Color.Black,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "File Size",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = NunitoFontFamily,
                                                    color = colors.textSubtle,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = FileUtils.formatSize(sizeBytes),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = NunitoFontFamily,
                                                    color = if (colors.isDark) Color.White else Color.Black,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        videoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                        )
                                    },
                                    border = BorderStroke(1.dp, colors.border),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.accent),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.SwapHoriz,
                                            contentDescription = "Change Video Icon",
                                            tint = colors.accent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Change Video",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = NunitoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = if (colors.isDark) Color.White else Color.Black
                                            )
                                        )
                                    }
                                }

                                Button(
                                    onClick = { onNavigateToPreview() },
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.accentPurple,
                                        contentColor = colors.onSecondaryContainer
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Visibility,
                                            contentDescription = "Preview Icon",
                                            tint = colors.onSecondaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Preview",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = NunitoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.onSecondaryContainer
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                                            putExtra(
                                                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                                                ComponentName(context, LivelyWallpaperService::class.java)
                                            )
                                        }
                                        context.startActivity(intent)
                                        showHomeScreenSuccessModal = true
                                    } catch (e: Exception) {
                                        // Fallback
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accent,
                                    contentColor = if (colors.isDark) colors.bg else Color.White
                                ),
                                shape = CircleShape,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .shadow(
                                        elevation = 8.dp,
                                        shape = CircleShape,
                                        ambientColor = colors.accent,
                                        spotColor = colors.accent
                                    )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Wallpaper,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "Apply Wallpaper",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = NunitoFontFamily,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(480.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(colors.surfaceContainerHigh),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CloudUpload,
                                        contentDescription = null,
                                        tint = colors.accent,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(32.dp))
                                Text(
                                    text = "Choose a Video",
                                    color = if (colors.isDark) Color.White else Color.Black,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontFamily = NunitoFontFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 28.sp,
                                        letterSpacing = (-0.5).sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Upload a local video and set it as a live wallpaper.",
                                    color = colors.textMuted,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = NunitoFontFamily,
                                        fontSize = 16.sp,
                                        lineHeight = 22.sp
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(40.dp))
                                Button(
                                    onClick = {
                                        videoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.accent,
                                        contentColor = if (colors.isDark) colors.bg else Color.White
                                    ),
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .height(56.dp)
                                        .padding(horizontal = 16.dp)
                                        .shadow(
                                            elevation = 8.dp,
                                            shape = CircleShape,
                                            ambientColor = colors.accent,
                                            spotColor = colors.accent
                                        )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "Choose Video",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontFamily = NunitoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showHomeScreenSuccessModal) {
        Dialog(onDismissRequest = { showHomeScreenSuccessModal = false }) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, colors.border, RoundedCornerShape(28.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Wallpaper Set Successfully!",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (colors.isDark) Color.White else Color.Black
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lively Wallpaper has been configured as your background. Your interactive, high-fidelity live video will now animate beautifully.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = NunitoFontFamily,
                            color = colors.textSubtle
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showHomeScreenSuccessModal = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            contentColor = if (colors.isDark) colors.bg else Color.White
                        ),
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Awesome",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = NunitoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}
