package com.beingniloy.lively.data.database

import androidx.room.*
import com.beingniloy.lively.data.model.CollectionWallpaperCrossRef
import com.beingniloy.lively.data.model.Wallpaper
import com.beingniloy.lively.data.model.WallpaperCollection
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperDao {
    @Query("SELECT * FROM wallpapers WHERE isRecent = 1 ORDER BY lastUsed DESC")
    fun getRecentWallpapers(): Flow<List<Wallpaper>>

    @Query("SELECT * FROM wallpapers WHERE isRecent = 1")
    suspend fun getRecentWallpapersOnce(): List<Wallpaper>

    @Query("SELECT * FROM wallpapers WHERE isFavorite = 1")
    fun getFavoriteWallpapers(): Flow<List<Wallpaper>>

    @Query("SELECT * FROM wallpapers WHERE isFavorite = 1")
    suspend fun getFavoriteWallpapersOnce(): List<Wallpaper>

    @Query("SELECT * FROM wallpapers")
    suspend fun getAllWallpapersOnce(): List<Wallpaper>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallpaper(wallpaper: Wallpaper)

    @Update
    suspend fun updateWallpaper(wallpaper: Wallpaper)

    @Delete
    suspend fun deleteWallpaper(wallpaper: Wallpaper)

    @Query("SELECT * FROM wallpapers WHERE id = :id")
    suspend fun getWallpaperById(id: String): Wallpaper?

    @Query("SELECT * FROM wallpapers WHERE filePath = :filePath")
    suspend fun getWallpaperByPath(filePath: String): Wallpaper?

    @Query("SELECT * FROM collections")
    fun getCollections(): Flow<List<WallpaperCollection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: WallpaperCollection)

    @Update
    suspend fun updateCollection(collection: WallpaperCollection)

    @Delete
    suspend fun deleteCollection(collection: WallpaperCollection)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWallpaperToCollection(crossRef: CollectionWallpaperCrossRef)

    @Query("DELETE FROM collection_wallpaper_cross_ref WHERE collectionId = :collectionId AND wallpaperId = :wallpaperId")
    suspend fun removeWallpaperFromCollection(collectionId: String, wallpaperId: String)

    @Query("""
        SELECT w.* FROM wallpapers w 
        INNER JOIN collection_wallpaper_cross_ref ref ON w.id = ref.wallpaperId 
        WHERE ref.collectionId = :collectionId
    """)
    fun getWallpapersInCollection(collectionId: String): Flow<List<Wallpaper>>
}
