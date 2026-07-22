package com.beingniloy.lively.ui.screens

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beingniloy.lively.LivelyWallpaperService
import com.beingniloy.lively.WallpaperState
import com.beingniloy.lively.WallpaperViewModel
import com.beingniloy.lively.ui.components.VideoPlayerPreview
import com.beingniloy.lively.ui.theme.LocalAppColors
import com.beingniloy.lively.ui.theme.NunitoFontFamily

@Composable
fun PreviewScreen(
    wallpaperViewModel: WallpaperViewModel,
    onNavigateBack: () -> Unit
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    val wallpaperState by wallpaperViewModel.wallpaperState.collectAsStateWithLifecycle()
    val fitMode by wallpaperViewModel.fitMode.collectAsStateWithLifecycle()
    val scale by wallpaperViewModel.scale.collectAsStateWithLifecycle()
    val positionX by wallpaperViewModel.positionX.collectAsStateWithLifecycle()
    val positionY by wallpaperViewModel.positionY.collectAsStateWithLifecycle()

    var isPlaying by remember { mutableStateOf(true) }
    var showTargetSheet by remember { mutableStateOf(false) }
    var showSuccessModal by remember { mutableStateOf(false) }
    var showExplanationDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    if (wallpaperState !is WallpaperState.Selected) {
        onNavigateBack()
        return
    }

    val videoPath = (wallpaperState as WallpaperState.Selected).filePath

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Toolbar with Back button and Reset
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.surfaceContainerLow)
            ) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = if (colors.isDark) Color.White else Color.Black)
            }
            
            Text(
                text = "Editor",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (colors.isDark) Color.White else Color.Black
                )
            )

            TextButton(
                onClick = { wallpaperViewModel.resetAdjustments() },
                colors = ButtonDefaults.textButtonColors(contentColor = colors.accent)
            ) {
                Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset", fontFamily = NunitoFontFamily, fontWeight = FontWeight.Bold)
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.Black)
                .border(1.dp, colors.border, RoundedCornerShape(32.dp))
        ) {
            VideoPlayerPreview(
                videoPath = videoPath,
                fitMode = fitMode,
                scale = scale,
                translationX = positionX * 300f,
                translationY = positionY * 500f,
                isPlaying = isPlaying,
                modifier = Modifier.fillMaxSize()
            )

            // Preview Status Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .graphicsLayer { alpha = pulseAlpha }
                            .clip(CircleShape)
                            .background(colors.accent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LIVE PREVIEW",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            fontFamily = NunitoFontFamily
                        )
                    )
                }
            }

            // Floating Play/Pause control button
            IconButton(
                onClick = { isPlaying = !isPlaying },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0x73000000))
                    .border(1.dp, Color(0x26FFFFFF), CircleShape)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "Toggle Play Pause",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Bottom controls card wrapper
        Card(
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.cardBg
            ),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .border(BorderStroke(1.dp, colors.border), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "FIT MODE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.textSubtle, 
                        fontWeight = FontWeight.Bold, 
                        letterSpacing = 1.5.sp,
                        fontFamily = NunitoFontFamily
                    ),
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.bg)
                        .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("fit" to "Fit", "fill" to "Fill", "center_crop" to "Center Crop").forEach { (mode, label) ->
                        val isSelected = fitMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) colors.accentPurple else Color.Transparent)
                                .clickable { wallpaperViewModel.setFitMode(mode) }
                                .padding(vertical = 12.dp)
                                .testTag("fit_mode_${mode}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label, 
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = NunitoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) colors.onSecondaryContainer else colors.textMuted
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ZOOM SCALE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.textSubtle, 
                            fontWeight = FontWeight.Bold, 
                            letterSpacing = 1.5.sp,
                            fontFamily = NunitoFontFamily
                        )
                    )
                    Text(
                        text = String.format("%.2fx", scale),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = colors.accent, 
                            fontWeight = FontWeight.Bold,
                            fontFamily = NunitoFontFamily
                        )
                    )
                }
                Slider(
                    value = scale,
                    onValueChange = { wallpaperViewModel.setScale(it) },
                    valueRange = 0.5f..3.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = colors.accent,
                        activeTrackColor = colors.accent,
                        inactiveTrackColor = colors.accentPurple
                    ),
                    modifier = Modifier.testTag("scale_slider")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "POSITION X / Y",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.textSubtle, 
                            fontWeight = FontWeight.Bold, 
                            letterSpacing = 1.5.sp,
                            fontFamily = NunitoFontFamily
                        )
                    )
                    Text(
                        text = String.format("X: %.1f, Y: %.1f", positionX, positionY),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = colors.accent, 
                            fontWeight = FontWeight.Bold,
                            fontFamily = NunitoFontFamily
                        )
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Slider(
                        value = positionX,
                        onValueChange = { wallpaperViewModel.setPositionX(it) },
                        valueRange = -1.0f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.accent,
                            activeTrackColor = colors.accent,
                            inactiveTrackColor = colors.accentPurple
                        ),
                        modifier = Modifier.weight(1f).testTag("position_x_slider")
                    )
                    Slider(
                        value = positionY,
                        onValueChange = { wallpaperViewModel.setPositionY(it) },
                        valueRange = -1.0f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.accent,
                            activeTrackColor = colors.accent,
                            inactiveTrackColor = colors.accentPurple
                        ),
                        modifier = Modifier.weight(1f).testTag("position_y_slider")
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showTargetSheet = true },
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
                        .testTag("set_live_wallpaper_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Wallpaper,
                            contentDescription = "Wallpaper Icon",
                            tint = if (colors.isDark) colors.bg else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Apply Wallpaper", 
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = NunitoFontFamily,
                                fontWeight = FontWeight.Bold, 
                                color = if (colors.isDark) colors.bg else Color.White,
                                fontSize = 16.sp
                            )
                        )
                    }
                }
            }
        }
    }

    if (showTargetSheet) {
        Dialog(onDismissRequest = { showTargetSheet = false }) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, colors.border, RoundedCornerShape(28.dp))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Set Live Wallpaper",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (colors.isDark) Color.White else Color.Black
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose where to apply this animated background.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = NunitoFontFamily,
                            color = colors.textSubtle
                        )
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    listOf(
                        "Home Screen" to "Apply to the main screen with interactive simulated dock widgets.",
                        "Lock Screen" to "Apply to the secure locked screen showcasing the Pixel display clock.",
                        "Home & Lock Screen" to "Mirror the video across both Home and Lock screens synchronously."
                    ).forEach { (title, subtitle) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    showTargetSheet = false
                                    try {
                                        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                                            putExtra(
                                                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                                                ComponentName(context, LivelyWallpaperService::class.java)
                                            )
                                        }
                                        context.startActivity(intent)
                                        showSuccessModal = true
                                    } catch (e: Exception) {
                                        showExplanationDialog = true
                                    }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(colors.accentPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = colors.onSecondaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = NunitoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (colors.isDark) Color.White else Color.Black
                                    )
                                )
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = NunitoFontFamily,
                                        color = colors.textSubtle
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Note: Separate lock screen live video wallpaper support is dependent on device capabilities.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = NunitoFontFamily,
                            color = colors.textMuted
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = { showTargetSheet = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Cancel", fontFamily = NunitoFontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showExplanationDialog) {
        AlertDialog(
            onDismissRequest = { showExplanationDialog = false },
            title = { Text("Lively Wallpaper Engine", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Your wallpaper scaling configurations have been cached successfully.\n\n" +
                    "To activate Lively Wallpaper on this simulator or a real device, please set it as the active wallpaper using your device Settings -> Wallpaper -> Live Wallpapers -> Lively Wallpaper.",
                    color = colors.textMuted
                )
            },
            confirmButton = {
                TextButton(onClick = { showExplanationDialog = false }) {
                    Text("Got It", fontWeight = FontWeight.Bold, color = colors.accent)
                }
            },
            containerColor = colors.cardBg
        )
    }

    if (showSuccessModal) {
        Dialog(onDismissRequest = { showSuccessModal = false }) {
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
                        onClick = { showSuccessModal = false },
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
