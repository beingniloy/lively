package com.beingniloy.lively

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.beingniloy.lively.data.database.LivelyDatabase
import com.beingniloy.lively.data.model.Wallpaper
import com.beingniloy.lively.data.model.WallpaperCollection
import com.beingniloy.lively.data.repository.WallpaperRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

sealed class WallpaperState {
    object Idle : WallpaperState()
    object Copying : WallpaperState()
    data class Selected(val fileName: String, val sizeBytes: Long, val filePath: String) : WallpaperState()
    data class Error(val message: String) : WallpaperState()
}

class WallpaperViewModel(application: Application) : AndroidViewModel(application) {
    private val database = LivelyDatabase.getDatabase(application)
    private val repository = WallpaperRepository(application, database.wallpaperDao())
    private val sharedPrefs = application.getSharedPreferences("lively_prefs", android.content.Context.MODE_PRIVATE)

    private val _wallpaperState = MutableStateFlow<WallpaperState>(WallpaperState.Idle)
    val wallpaperState: StateFlow<WallpaperState> = _wallpaperState.asStateFlow()

    // Scaling preferences
    private val _fitMode = MutableStateFlow(sharedPrefs.getString("fit_mode", "center_crop") ?: "center_crop")
    val fitMode: StateFlow<String> = _fitMode.asStateFlow()

    private val _scale = MutableStateFlow(sharedPrefs.getFloat("scale", 1.0f))
    val scale: StateFlow<Float> = _scale.asStateFlow()

    private val _positionX = MutableStateFlow(sharedPrefs.getFloat("position_x", 0.0f))
    val positionX: StateFlow<Float> = _positionX.asStateFlow()

    private val _positionY = MutableStateFlow(sharedPrefs.getFloat("position_y", 0.0f))
    val positionY: StateFlow<Float> = _positionY.asStateFlow()

    // Recent wallpapers
    val recentWallpapers: StateFlow<List<Wallpaper>> = repository.recentWallpapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Favorites
    val favorites: StateFlow<List<Wallpaper>> = repository.favoriteWallpapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Collections
    val collections: StateFlow<List<WallpaperCollection>> = repository.collections
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Wallpaper configuration preferences
    private val _batterySaver = MutableStateFlow(sharedPrefs.getBoolean("battery_saver", false))
    val batterySaver: StateFlow<Boolean> = _batterySaver.asStateFlow()

    private val _loopPlayback = MutableStateFlow(sharedPrefs.getBoolean("loop_playback", true))
    val loopPlayback: StateFlow<Boolean> = _loopPlayback.asStateFlow()

    private val _previewQuality = MutableStateFlow(sharedPrefs.getString("preview_quality", "high") ?: "high")
    val previewQuality: StateFlow<String> = _previewQuality.asStateFlow()

    private val _cacheSize = MutableStateFlow("0.00 B")
    val cacheSize: StateFlow<String> = _cacheSize.asStateFlow()

    private val _storageSize = MutableStateFlow("0.00 B")
    val storageSize: StateFlow<String> = _storageSize.asStateFlow()

    init {
        checkCurrentWallpaper()
        updateSizes()
    }

    fun checkCurrentWallpaper() {
        val file = File(getApplication<Application>().filesDir, "active_wallpaper.mp4")
        if (file.exists()) {
            val savedName = sharedPrefs.getString("video_name", "active_wallpaper.mp4") ?: "active_wallpaper.mp4"
            _wallpaperState.value = WallpaperState.Selected(
                fileName = savedName,
                sizeBytes = file.length(),
                filePath = file.absolutePath
            )
        } else {
            _wallpaperState.value = WallpaperState.Idle
        }
    }

    fun selectVideo(uri: Uri) {
        viewModelScope.launch {
            _wallpaperState.value = WallpaperState.Copying
            val result = repository.importWallpaper(uri)
            result.onSuccess {
                val timestamp = System.currentTimeMillis()
                sharedPrefs.edit().apply {
                    putString("video_name", it.fileName)
                    putLong("video_size", it.sizeBytes)
                    putLong("wallpaper_updated", timestamp)
                    apply()
                }
                checkCurrentWallpaper()
            }.onFailure {
                _wallpaperState.value = WallpaperState.Error(it.message ?: "Failed to import video")
            }
        }
    }

    fun applyRecentWallpaper(item: Wallpaper) {
        viewModelScope.launch {
            repository.applyWallpaper(item)
            val timestamp = System.currentTimeMillis()
            sharedPrefs.edit().apply {
                putString("video_name", item.fileName)
                putLong("video_size", item.sizeBytes)
                putLong("wallpaper_updated", timestamp)
                apply()
            }
            checkCurrentWallpaper()
        }
    }

    fun deleteRecentWallpaper(item: Wallpaper) {
        viewModelScope.launch {
            repository.deleteWallpaper(item)
        }
    }

    fun setFitMode(mode: String) {
        _fitMode.value = mode
        sharedPrefs.edit().putString("fit_mode", mode).apply()
    }

    fun setScale(newScale: Float) {
        _scale.value = newScale
        sharedPrefs.edit().putFloat("scale", newScale).apply()
    }

    fun setPositionX(value: Float) {
        _positionX.value = value
        sharedPrefs.edit().putFloat("position_x", value).apply()
    }

    fun setPositionY(value: Float) {
        _positionY.value = value
        sharedPrefs.edit().putFloat("position_y", value).apply()
    }

    fun resetAdjustments() {
        setFitMode("center_crop")
        setScale(1.0f)
        setPositionX(0.0f)
        setPositionY(0.0f)
    }

    fun toggleFavorite(item: Wallpaper) {
        viewModelScope.launch {
            repository.toggleFavorite(item)
        }
    }

    fun isFavorite(filePath: String): Boolean {
        return favorites.value.any { it.filePath == filePath }
    }

    fun createCollection(name: String) {
        viewModelScope.launch {
            repository.createCollection(name)
        }
    }

    fun deleteCollection(collection: WallpaperCollection) {
        viewModelScope.launch {
            repository.deleteCollection(collection)
        }
    }

    fun addVideoToCollection(collectionId: String, wallpaper: Wallpaper) {
        viewModelScope.launch {
            repository.addWallpaperToCollection(collectionId, wallpaper.id)
        }
    }

    fun removeVideoFromCollection(collectionId: String, wallpaperId: String) {
        viewModelScope.launch {
            repository.removeWallpaperFromCollection(collectionId, wallpaperId)
        }
    }

    fun getWallpapersInCollection(collectionId: String): Flow<List<Wallpaper>> {
        return repository.getWallpapersInCollection(collectionId)
    }

    fun deleteCurrentWallpaper() {
        val destFile = File(getApplication<Application>().filesDir, "active_wallpaper.mp4")
        if (destFile.exists()) {
            destFile.delete()
        }
        sharedPrefs.edit().apply {
            remove("video_name")
            remove("video_size")
            putLong("wallpaper_updated", System.currentTimeMillis())
            apply()
        }
        _wallpaperState.value = WallpaperState.Idle
    }

    fun setBatterySaver(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("battery_saver", enabled).apply()
        _batterySaver.value = enabled
    }

    fun setLoopPlayback(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("loop_playback", enabled).apply()
        _loopPlayback.value = enabled
    }

    fun setPreviewQuality(quality: String) {
        sharedPrefs.edit().putString("preview_quality", quality).apply()
        _previewQuality.value = quality
    }

    fun clearRecentWallpapers() {
        viewModelScope.launch {
            repository.clearRecents()
            updateSizes()
        }
    }

    fun updateSizes() {
        viewModelScope.launch {
            _cacheSize.value = repository.getCacheSize()
            _storageSize.value = repository.getStorageSize()
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            repository.clearCache()
            updateSizes()
        }
    }

    fun clearFavorites() {
        viewModelScope.launch {
            repository.clearFavorites()
            updateSizes()
        }
    }

    fun clearAllStorage() {
        viewModelScope.launch {
            repository.clearAllStorage()
            deleteCurrentWallpaper()
            updateSizes()
        }
    }
}
