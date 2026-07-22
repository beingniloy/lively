package com.beingniloy.lively.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beingniloy.lively.WallpaperViewModel
import com.beingniloy.lively.data.model.Wallpaper
import com.beingniloy.lively.ui.theme.AppColors
import com.beingniloy.lively.ui.theme.NunitoFontFamily

@Composable
fun LibraryVideoCard(
    item: Wallpaper,
    colors: AppColors,
    wallpaperViewModel: WallpaperViewModel,
    onPreview: () -> Unit,
    onAddToCollection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFav = wallpaperViewModel.isFavorite(item.filePath)
    
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .background(Color.Black)
            ) {
                VideoPlayerPreview(
                    videoPath = item.filePath,
                    fitMode = "center_crop",
                    scale = 1.0f,
                    modifier = Modifier.fillMaxSize()
                )
                
                IconButton(
                    onClick = { wallpaperViewModel.toggleFavorite(item) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(32.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFav) Color(0xFFEF4444) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.fileName,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = NunitoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = if (colors.isDark) Color.White else Color.Black
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                val sizeStr = remember(item.sizeBytes) {
                    val mb = item.sizeBytes / (1024 * 1024f)
                    String.format("%.1f MB", mb)
                }
                
                Text(
                    text = "${item.resolution} • $sizeStr",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = NunitoFontFamily,
                        color = colors.textSubtle
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = onPreview,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, colors.border),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.accent),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        Text("Edit", fontFamily = NunitoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    
                    Button(
                        onClick = onAddToCollection,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = if (colors.isDark) colors.bg else Color.White),
                        modifier = Modifier.weight(1.2f),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        Text("+ Col", fontFamily = NunitoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
