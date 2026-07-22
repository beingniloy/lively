package com.beingniloy.lively.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beingniloy.lively.PreferencesViewModel
import com.beingniloy.lively.WallpaperViewModel
import com.beingniloy.lively.data.model.Wallpaper
import com.beingniloy.lively.data.model.WallpaperCollection
import com.beingniloy.lively.ui.components.LibraryVideoCard
import com.beingniloy.lively.ui.components.VideoPlayerPreview
import com.beingniloy.lively.ui.theme.LocalAppColors
import com.beingniloy.lively.ui.theme.NunitoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    wallpaperViewModel: WallpaperViewModel,
    preferencesViewModel: PreferencesViewModel,
    onNavigateToPreview: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val colors = LocalAppColors.current
    val collections by wallpaperViewModel.collections.collectAsStateWithLifecycle()
    val recentWallpapers by wallpaperViewModel.recentWallpapers.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf("Recently Used") }
    var showCreateCollectionDialog by remember { mutableStateOf(false) }
    var newCollectionName by remember { mutableStateOf("") }
    var selectedCollectionForDetail by remember { mutableStateOf<WallpaperCollection?>(null) }
    var selectedVideoForAddToCollection by remember { mutableStateOf<Wallpaper?>(null) }
    var showAddToCollectionDialog by remember { mutableStateOf(false) }
    var expandedSortMenu by remember { mutableStateOf(false) }

    val filteredCollections = remember(collections, searchQuery) {
        collections.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }
    val sortedVideos = remember(recentWallpapers, sortOption, searchQuery) {
        val filtered = recentWallpapers.filter { it.fileName.contains(searchQuery, ignoreCase = true) }
        when (sortOption) {
            "Name" -> filtered.sortedBy { it.fileName }
            "File Size" -> filtered.sortedByDescending { it.sizeBytes }
            "Duration" -> filtered.sortedByDescending { it.duration }
            else -> filtered.sortedByDescending { it.lastUsed }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Library",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search videos and collections...", fontFamily = NunitoFontFamily, color = colors.textSubtle) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search", tint = colors.accent) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Rounded.Close, contentDescription = "Clear") } }
                    } else null,
                    modifier = Modifier.fillMaxWidth().testTag("library_search_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = colors.accent,
                        unfocusedIndicatorColor = colors.border
                    ),
                    singleLine = true
                )
            }

            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Collections",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = NunitoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (colors.isDark) Color.White else Color.Black
                            )
                        )
                        TextButton(
                            onClick = { showCreateCollectionDialog = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = colors.accent)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add Collection", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Create", fontFamily = NunitoFontFamily, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (filteredCollections.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(colors.surfaceContainerLow),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No collections found", fontFamily = NunitoFontFamily, color = colors.textSubtle)
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(filteredCollections) { col ->
                                Card(
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
                                    modifier = Modifier
                                        .width(150.dp)
                                        .height(120.dp)
                                        .clickable { selectedCollectionForDetail = col }
                                        .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(colors.accentPurple),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Folder,
                                                contentDescription = null,
                                                tint = colors.onSecondaryContainer,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = col.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = NunitoFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (colors.isDark) Color.White else Color.Black
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            // Since items are loaded asynchronously, we might need a better way to show count
                                            // For now, we'll keep it simple
                                            Text(
                                                text = "Videos",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = NunitoFontFamily,
                                                    color = colors.textSubtle
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

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All Videos",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (colors.isDark) Color.White else Color.Black
                        )
                    )
                    Box {
                        FilterChip(
                            selected = true,
                            onClick = { expandedSortMenu = true },
                            label = { Text("Sort: $sortOption", fontFamily = NunitoFontFamily, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.accentPurple,
                                selectedLabelColor = colors.onSecondaryContainer
                            ),
                            border = BorderStroke(1.dp, colors.border),
                            shape = CircleShape
                        )
                        DropdownMenu(
                            expanded = expandedSortMenu,
                            onDismissRequest = { expandedSortMenu = false },
                            modifier = Modifier.background(colors.cardBg)
                        ) {
                            listOf("Recently Used", "Name", "File Size", "Duration").forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt, fontFamily = NunitoFontFamily) },
                                    onClick = {
                                        sortOption = opt
                                        expandedSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (sortedVideos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No videos found", fontFamily = NunitoFontFamily, color = colors.textSubtle)
                    }
                }
            } else {
                val chunked = sortedVideos.chunked(2)
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
                                    selectedVideoForAddToCollection = item
                                    showAddToCollectionDialog = true
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

        if (showCreateCollectionDialog) {
            AlertDialog(
                onDismissRequest = { showCreateCollectionDialog = false },
                title = { Text("Create Collection", fontFamily = NunitoFontFamily, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newCollectionName,
                        onValueChange = { newCollectionName = it },
                        label = { Text("Collection Name", fontFamily = NunitoFontFamily) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newCollectionName.isNotEmpty()) {
                                wallpaperViewModel.createCollection(newCollectionName)
                                newCollectionName = ""
                                showCreateCollectionDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                    ) {
                        Text("Create", fontFamily = NunitoFontFamily, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateCollectionDialog = false }) {
                        Text("Cancel", fontFamily = NunitoFontFamily)
                    }
                },
                containerColor = colors.cardBg
            )
        }

        if (showAddToCollectionDialog && selectedVideoForAddToCollection != null) {
            Dialog(onDismissRequest = { showAddToCollectionDialog = false }) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .border(1.dp, colors.border, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Add to Collection",
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = NunitoFontFamily, fontWeight = FontWeight.ExtraBold)
                        )
                        Text(
                            text = "Choose a collection to save ${selectedVideoForAddToCollection!!.fileName}.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = NunitoFontFamily, color = colors.textSubtle),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 240.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(collections) { col ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            wallpaperViewModel.addVideoToCollection(col.id, selectedVideoForAddToCollection!!)
                                            showAddToCollectionDialog = false
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.Folder, contentDescription = null, tint = colors.accent, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = col.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = NunitoFontFamily, fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showAddToCollectionDialog = false }) {
                                Text("Close", fontFamily = NunitoFontFamily)
                            }
                        }
                    }
                }
            }
        }

        if (selectedCollectionForDetail != null) {
            val collectionId = selectedCollectionForDetail!!.id
            val collectionWallpapers by wallpaperViewModel.getWallpapersInCollection(collectionId).collectAsStateWithLifecycle(emptyList())

            Dialog(onDismissRequest = { selectedCollectionForDetail = null }) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.bg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f)
                        .border(1.dp, colors.border, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = selectedCollectionForDetail!!.name,
                                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = NunitoFontFamily, fontWeight = FontWeight.ExtraBold)
                                )
                                Text(
                                    text = "Collection • ${collectionWallpapers.size} Videos",
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = NunitoFontFamily, color = colors.textSubtle)
                                )
                            }
                            IconButton(onClick = { selectedCollectionForDetail = null }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Close")
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (collectionWallpapers.isEmpty()) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("This collection is empty", fontFamily = NunitoFontFamily, color = colors.textSubtle)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(collectionWallpapers) { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(colors.surfaceContainerLow)
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(Color.Black)) {
                                            VideoPlayerPreview(
                                                videoPath = item.filePath,
                                                fitMode = "center_crop",
                                                scale = 1.0f,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.fileName,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = NunitoFontFamily, fontWeight = FontWeight.Bold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = item.resolution,
                                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = NunitoFontFamily, color = colors.textSubtle)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                wallpaperViewModel.removeVideoFromCollection(collectionId, item.id)
                                            }
                                        ) {
                                            Icon(Icons.Rounded.Delete, contentDescription = "Remove", tint = Color(0xFFDC2626))
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    wallpaperViewModel.deleteCollection(selectedCollectionForDetail!!)
                                    selectedCollectionForDetail = null
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                modifier = Modifier.weight(1f),
                                shape = CircleShape
                            ) {
                                Text("Delete Collection", fontFamily = NunitoFontFamily, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
