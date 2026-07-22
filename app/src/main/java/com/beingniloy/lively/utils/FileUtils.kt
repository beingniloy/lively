package com.beingniloy.lively.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import java.io.File

object FileUtils {
    fun getFileNameAndSize(context: Context, uri: Uri): Pair<String, Long> {
        var sizeBytes: Long = 0
        var displayName = "Selected Video"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    sizeBytes = cursor.getLong(sizeIndex)
                }
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    displayName = cursor.getString(nameIndex) ?: "Selected Video"
                }
            }
        }
        return Pair(displayName, sizeBytes)
    }

    fun copyUriToFile(context: Context, uri: Uri, destFile: File): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            Log.e("FileUtils", "Error copying URI to file", e)
            false
        }
    }

    fun getVideoMetadata(filePath: String): Pair<String, String> {
        var durationVal = "00:05"
        var resolutionVal = "1080p"
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(filePath)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val durationMs = durationStr?.toLongOrNull() ?: 0L
            val seconds = (durationMs / 1000) % 60
            val minutes = (durationMs / (1000 * 60)) % 60
            durationVal = String.format("%02d:%02d", minutes, seconds)
            resolutionVal = if (width != null && height != null) "${width}x${height}" else "1080p"
        } catch (e: Exception) {
            Log.e("FileUtils", "Metadata extraction failed", e)
        } finally {
            try {
                retriever.release()
            } catch (ex: Exception) { /* ignored */ }
        }
        return Pair(durationVal, resolutionVal)
    }

    fun formatSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
        val pre = "KMGTPE"[exp - 1]
        return String.format("%.1f %sB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
    }
}
