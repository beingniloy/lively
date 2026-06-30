package com.beingniloy.lively.ui.components

import androidx.annotation.OptIn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

@OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoPlayerPreview(
    videoPath: String,
    fitMode: String,
    scale: Float,
    translationX: Float = 0f,
    translationY: Float = 0f,
    isPlaying: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentIsPlaying by rememberUpdatedState(isPlaying)
    val lifecycleOwner = LocalLifecycleOwner.current

    val player = remember(videoPath) {
        ExoPlayer.Builder(context.applicationContext).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f
            setMediaItem(MediaItem.fromUri(videoPath))
            prepare()
            
            // Set initial scaling mode
            videoScalingMode = when (fitMode) {
                "fit" -> C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                "fill" -> C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                else -> C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            }
            
            addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    android.util.Log.e("Lively", "Preview player error: ${error.message}")
                }
            })
        }
    }

    DisposableEffect(player, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (currentIsPlaying) player.play()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    player.pause()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                player.stop()
                player.clearMediaItems()
                player.release()
            } catch (e: Exception) {
                android.util.Log.e("Lively", "Error releasing player: ${e.message}")
            }
        }
    }

    LaunchedEffect(player, isPlaying) {
        if (isPlaying) player.play() else player.pause()
    }

    LaunchedEffect(player, fitMode) {
        player.videoScalingMode = when (fitMode) {
            "fit" -> C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            "fill" -> C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            else -> C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        }
    }

    AndroidView(
        factory = { ctx ->
            androidx.media3.ui.PlayerView(ctx).apply {
                useController = false
                this.player = player
                setBackgroundColor(android.graphics.Color.BLACK)
            }
        },
        update = { view ->
            if (view.player != player) {
                view.player = player
            }
        },
        modifier = modifier.graphicsLayer(
            scaleX = scale.coerceIn(0.1f, 10f),
            scaleY = scale.coerceIn(0.1f, 10f),
            translationX = translationX,
            translationY = translationY
        )
    )
}
