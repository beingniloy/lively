package com.beingniloy.lively.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallpapers")
data class Wallpaper(
    @PrimaryKey val id: String,
    val fileName: String,
    val filePath: String,
    val sizeBytes: Long,
    val duration: String,
    val resolution: String,
    val lastUsed: Long,
    val isRecent: Boolean = false,
    val isFavorite: Boolean = false
)

@Entity(tableName = "collections")
data class WallpaperCollection(
    @PrimaryKey val id: String,
    val name: String
)

@Entity(tableName = "collection_wallpaper_cross_ref", primaryKeys = ["collectionId", "wallpaperId"])
data class CollectionWallpaperCrossRef(
    val collectionId: String,
    val wallpaperId: String
)
