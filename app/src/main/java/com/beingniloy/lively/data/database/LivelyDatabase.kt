package com.beingniloy.lively.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.beingniloy.lively.data.model.CollectionWallpaperCrossRef
import com.beingniloy.lively.data.model.Wallpaper
import com.beingniloy.lively.data.model.WallpaperCollection

@Database(
    entities = [Wallpaper::class, WallpaperCollection::class, CollectionWallpaperCrossRef::class],
    version = 1,
    exportSchema = false
)
abstract class LivelyDatabase : RoomDatabase() {
    abstract fun wallpaperDao(): WallpaperDao

    companion object {
        @Volatile
        private var INSTANCE: LivelyDatabase? = null

        fun getDatabase(context: Context): LivelyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LivelyDatabase::class.java,
                    "lively_database"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
