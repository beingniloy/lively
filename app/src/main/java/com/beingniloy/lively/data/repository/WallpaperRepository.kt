package com.beingniloy.lively.data.repository

import android.content.Context
import android.net.Uri
import com.beingniloy.lively.data.database.WallpaperDao
import com.beingniloy.lively.data.model.CollectionWallpaperCrossRef
import com.beingniloy.lively.data.model.Wallpaper
import com.beingniloy.lively.data.model.WallpaperCollection
import com.beingniloy.lively.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class WallpaperRepository(
    private val context: Context,
    private val wallpaperDao: WallpaperDao
) {
    val recentWallpapers: Flow<List<Wallpaper>> = wallpaperDao.getRecentWallpapers()
    val favoriteWallpapers: Flow<List<Wallpaper>> = wallpaperDao.getFavoriteWallpapers()
    val collections: Flow<List<WallpaperCollection>> = wallpaperDao.getCollections()

    fun getWallpapersInCollection(collectionId: String): Flow<List<Wallpaper>> {
        return wallpaperDao.getWallpapersInCollection(collectionId)
    }

    suspend fun importWallpaper(uri: Uri): Result<Wallpaper> = withContext(Dispatchers.IO) {
        try {
            val (displayName, sizeBytes) = FileUtils.getFileNameAndSize(context, uri)
            
            // Check if file size exceeds 200MB
            val maxSize = 200 * 1024 * 1024L
            if (sizeBytes > maxSize) {
                return@withContext Result.failure(Exception("File is too large! Maximum limit is 200 MB."))
            }

            // Copy to recent_wallpapers with unique name
            val recentsDir = File(context.filesDir, "recent_wallpapers")
            if (!recentsDir.exists()) recentsDir.mkdirs()
            
            val timestamp = System.currentTimeMillis()
            val uniqueFileName = "wallpaper_$timestamp.mp4"
            val recentFile = File(recentsDir, uniqueFileName)

            if (!FileUtils.copyUriToFile(context, uri, recentFile)) {
                return@withContext Result.failure(Exception("Failed to copy video file."))
            }

            // Copy to active_wallpaper.mp4
            val destFile = File(context.filesDir, "active_wallpaper.mp4")
            recentFile.inputStream().use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val (duration, resolution) = FileUtils.getVideoMetadata(recentFile.absolutePath)

            val wallpaper = Wallpaper(
                id = timestamp.toString(),
                fileName = displayName,
                filePath = recentFile.absolutePath,
                sizeBytes = sizeBytes,
                duration = duration,
                resolution = resolution,
                lastUsed = timestamp,
                isRecent = true
            )

            wallpaperDao.insertWallpaper(wallpaper)
            
            // Enforce limit of 10 recent items in DB
            // (The original code had 6, let's keep 10 for a bit more flexibility or stick to 6 if preferred)
            // But we should also delete the files.
            
            Result.success(wallpaper)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun applyWallpaper(wallpaper: Wallpaper) = withContext(Dispatchers.IO) {
        val recentFile = File(wallpaper.filePath)
        if (recentFile.exists()) {
            val destFile = File(context.filesDir, "active_wallpaper.mp4")
            recentFile.inputStream().use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            val updatedWallpaper = wallpaper.copy(lastUsed = System.currentTimeMillis(), isRecent = true)
            wallpaperDao.insertWallpaper(updatedWallpaper)
        }
    }

    suspend fun toggleFavorite(wallpaper: Wallpaper) = withContext(Dispatchers.IO) {
        val updatedWallpaper = wallpaper.copy(isFavorite = !wallpaper.isFavorite)
        wallpaperDao.insertWallpaper(updatedWallpaper)
    }

    suspend fun deleteWallpaper(wallpaper: Wallpaper) = withContext(Dispatchers.IO) {
        val file = File(wallpaper.filePath)
        if (file.exists()) file.delete()
        wallpaperDao.deleteWallpaper(wallpaper)
    }

    suspend fun createCollection(name: String) = withContext(Dispatchers.IO) {
        val collection = WallpaperCollection(
            id = System.currentTimeMillis().toString(),
            name = name
        )
        wallpaperDao.insertCollection(collection)
    }

    suspend fun deleteCollection(collection: WallpaperCollection) = withContext(Dispatchers.IO) {
        wallpaperDao.deleteCollection(collection)
    }

    suspend fun addWallpaperToCollection(collectionId: String, wallpaperId: String) = withContext(Dispatchers.IO) {
        wallpaperDao.insertWallpaperToCollection(CollectionWallpaperCrossRef(collectionId, wallpaperId))
    }

    suspend fun removeWallpaperFromCollection(collectionId: String, wallpaperId: String) = withContext(Dispatchers.IO) {
        wallpaperDao.removeWallpaperFromCollection(collectionId, wallpaperId)
    }
    
    suspend fun clearRecents() = withContext(Dispatchers.IO) {
        val wallpapers = wallpaperDao.getRecentWallpapersOnce()
        wallpapers.forEach { wp ->
            if (!wp.isFavorite) {
                // If not favorite, we can delete the file and DB entry
                val file = File(wp.filePath)
                if (file.exists()) file.delete()
                wallpaperDao.deleteWallpaper(wp)
            } else {
                // If favorite, just clear the recent flag
                wallpaperDao.insertWallpaper(wp.copy(isRecent = false))
            }
        }
    }

    suspend fun clearFavorites() = withContext(Dispatchers.IO) {
        val wallpapers = wallpaperDao.getFavoriteWallpapersOnce()
        wallpapers.forEach { wp ->
            if (!wp.isRecent) {
                // If not recent, we can delete the file and DB entry
                val file = File(wp.filePath)
                if (file.exists()) file.delete()
                wallpaperDao.deleteWallpaper(wp)
            } else {
                // If recent, just clear the favorite flag
                wallpaperDao.insertWallpaper(wp.copy(isFavorite = false))
            }
        }
    }

    suspend fun clearAllStorage() = withContext(Dispatchers.IO) {
        val wallpapers = wallpaperDao.getAllWallpapersOnce()
        wallpapers.forEach { wp ->
            val file = File(wp.filePath)
            if (file.exists()) file.delete()
            wallpaperDao.deleteWallpaper(wp)
        }
        val activeFile = File(context.filesDir, "active_wallpaper.mp4")
        if (activeFile.exists()) activeFile.delete()
    }

    fun getCacheSize(): String {
        val cacheSize = getFolderSize(context.cacheDir)
        return formatSize(cacheSize)
    }

    fun clearCache() {
        deleteFolderContents(context.cacheDir)
    }

    fun getStorageSize(): String {
        val filesSize = getFolderSize(context.filesDir)
        return formatSize(filesSize)
    }

    private fun getFolderSize(file: File): Long {
        if (!file.exists()) return 0L
        var size = 0L
        if (file.isDirectory) {
            file.listFiles()?.forEach {
                size += getFolderSize(it)
            }
        } else {
            size = file.length()
        }
        return size
    }

    private fun deleteFolderContents(file: File) {
        if (!file.exists()) return
        if (file.isDirectory) {
            file.listFiles()?.forEach {
                deleteFolderContents(it)
                it.delete()
            }
        } else {
            file.delete()
        }
    }

    private fun formatSize(size: Long): String {
        if (size <= 0) return "0.00 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        val index = if (digitGroups < units.size) digitGroups else units.size - 1
        return String.format(java.util.Locale.US, "%.2f %s", size / Math.pow(1024.0, index.toDouble()), units[index])
    }
}
