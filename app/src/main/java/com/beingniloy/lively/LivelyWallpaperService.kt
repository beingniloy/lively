@file:OptIn(UnstableApi::class)
package com.beingniloy.lively

import android.content.Context
import android.content.SharedPreferences
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import java.io.File

class LivelyWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return VideoEngine()
    }

    inner class VideoEngine : Engine() {
        private var player: ExoPlayer? = null
        private var isPlayerVisible = false

        private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "fit_mode" || key == "scale" || key == "wallpaper_updated" || key == "loop_playback") {
                val p = player ?: return@OnSharedPreferenceChangeListener
                val prefs = this@LivelyWallpaperService.getSharedPreferences("lively_prefs", Context.MODE_PRIVATE)
                
                try {
                    if (key == "loop_playback") {
                        val loop = prefs.getBoolean("loop_playback", true)
                        p.repeatMode = if (loop) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
                    }

                    val fitMode = prefs.getString("fit_mode", "center_crop") ?: "center_crop"
                    
                    when (fitMode) {
                        "fit" -> p.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                        "fill" -> p.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                        else -> p.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                    }
                    
                    if (surfaceHolder.surface.isValid) {
                        adjustSurfaceSize(surfaceHolder)
                    }
                    
                    if (key == "wallpaper_updated") {
                        val videoFile = File(this@LivelyWallpaperService.filesDir, "active_wallpaper.mp4")
                        if (videoFile.exists()) {
                            p.stop()
                            p.setMediaItem(MediaItem.fromUri(videoFile.absolutePath))
                            p.prepare()
                            if (isPlayerVisible) {
                                p.play()
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("Lively", "Error in preference change listener: ${e.message}")
                }
            }
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            val context = this@LivelyWallpaperService
            val videoFile = File(context.filesDir, "active_wallpaper.mp4")
            
            val prefs = context.getSharedPreferences("lively_prefs", Context.MODE_PRIVATE)
            prefs.registerOnSharedPreferenceChangeListener(prefListener)

            player = ExoPlayer.Builder(context).build().apply {
                val loop = prefs.getBoolean("loop_playback", true)
                repeatMode = if (loop) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
                volume = 0f // Muted playback
                
                val fitMode = prefs.getString("fit_mode", "center_crop") ?: "center_crop"
                when (fitMode) {
                    "fit" -> videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                    "fill" -> videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                    else -> videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                }
                
                setVideoSurfaceHolder(holder)

                if (videoFile.exists()) {
                    setMediaItem(MediaItem.fromUri(videoFile.absolutePath))
                    prepare()
                    if (isPlayerVisible) {
                        play()
                    }
                }
            }
            adjustSurfaceSize(holder)
        }

        private fun adjustSurfaceSize(holder: SurfaceHolder) {
            val p = player ?: return
            if (!holder.surface.isValid) return
            
            try {
                val context = this@LivelyWallpaperService
                val prefs = context.getSharedPreferences("lively_prefs", Context.MODE_PRIVATE)
                val fitMode = prefs.getString("fit_mode", "center_crop") ?: "center_crop"
                val scale = prefs.getFloat("scale", 1.0f).coerceIn(0.1f, 10f)

                if (fitMode == "fill") {
                    val videoSize = p.videoSize
                    if (videoSize.width > 0 && videoSize.height > 0) {
                        val w = (videoSize.width / scale).toInt().coerceAtLeast(100)
                        val h = (videoSize.height / scale).toInt().coerceAtLeast(100)
                        holder.setFixedSize(w, h)
                    } else {
                        holder.setSizeFromLayout()
                    }
                } else {
                    holder.setSizeFromLayout()
                }
            } catch (e: Exception) {
                android.util.Log.e("Lively", "Error adjusting surface size: ${e.message}")
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            isPlayerVisible = visible
            if (visible) {
                val p = player ?: return
                val prefs = this@LivelyWallpaperService.getSharedPreferences("lively_prefs", Context.MODE_PRIVATE)
                val batterySaverPref = prefs.getBoolean("battery_saver", false)
                var shouldPause = false
                
                if (batterySaverPref) {
                    val powerManager = getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
                    if (powerManager?.isPowerSaveMode == true) {
                        shouldPause = true
                    } else {
                        val filter = android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
                        val batteryStatus = registerReceiver(null, filter)
                        val level = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
                        val scale = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
                        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()) else 100f
                        if (batteryPct < 15f) {
                            shouldPause = true
                        }
                    }
                }

                if (shouldPause) {
                    p.pause()
                } else {
                    p.play()
                }
            } else {
                player?.pause()
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            val prefs = this@LivelyWallpaperService.getSharedPreferences("lively_prefs", Context.MODE_PRIVATE)
            prefs.unregisterOnSharedPreferenceChangeListener(prefListener)
            player?.stop()
            player?.clearMediaItems()
            player?.release()
            player = null
            super.onSurfaceDestroyed(holder)
        }
    }
}
