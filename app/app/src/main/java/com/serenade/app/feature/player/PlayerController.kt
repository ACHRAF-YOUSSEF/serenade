package com.serenade.app.feature.player

import android.content.Context
import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.serenade.app.BuildConfig
import com.serenade.app.feature.player.service.SerenadePlayerService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class PlaybackItem(
    val trackId: String,
    val streamUrl: String,
)

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentTrackId: String? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val hasPrevious: Boolean = false,
    val hasNext: Boolean = false,
    val queueIndex: Int = 0,
    val queueSize: Int = 0,
)

@Singleton
class PlayerController @Inject constructor(
    val player: ExoPlayer,
    @param:ApplicationContext private val context: Context,
) {
    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state

    init {
        player.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                syncPlaybackState()
            }
        })
    }

    fun play(trackId: String, streamUrl: String) {
        playQueue(listOf(PlaybackItem(trackId, streamUrl)))
    }

    fun playQueue(items: List<PlaybackItem>, startIndex: Int = 0) {
        if (items.isEmpty()) return
        context.startService(Intent(context, SerenadePlayerService::class.java))
        val mediaItems = items.map { it.toMediaItem() }
        val boundedIndex = startIndex.coerceIn(mediaItems.indices)
        player.setMediaItems(mediaItems, boundedIndex, 0L)
        player.prepare()
        player.play()
        syncPlaybackState()
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
        syncPlaybackState()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        syncPlaybackState()
    }

    fun skipToPrevious() {
        if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
            player.play()
            syncPlaybackState()
        }
    }

    fun skipToNext() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
            player.play()
            syncPlaybackState()
        }
    }

    fun stop() {
        player.stop()
        _state.value = PlaybackState()
    }

    fun syncPosition() {
        if (_state.value.currentTrackId == null) return
        syncPlaybackState()
    }

    fun release() {
        player.release()
    }

    private fun syncPlaybackState() {
        val mediaItemCount = player.mediaItemCount
        val queueIndex = if (mediaItemCount > 0) {
            player.currentMediaItemIndex.coerceAtLeast(0)
        } else {
            0
        }
        _state.value = PlaybackState(
            isPlaying = player.isPlaying,
            currentTrackId = player.currentMediaItem?.mediaId,
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = player.duration.coerceAtLeast(0L),
            hasPrevious = player.hasPreviousMediaItem(),
            hasNext = player.hasNextMediaItem(),
            queueIndex = queueIndex,
            queueSize = mediaItemCount,
        )
    }

    private fun PlaybackItem.toMediaItem(): MediaItem {
        val resolvedUrl = streamUrl.resolvePlaybackUrl()
        val builder = MediaItem.Builder()
            .setMediaId(trackId)
            .setUri(resolvedUrl)
        if (resolvedUrl.isHlsManifestUrl()) {
            builder.setMimeType(MimeTypes.APPLICATION_M3U8)
        }
        return builder.build()
    }

    private fun String.resolvePlaybackUrl(): String {
        if (startsWith("http://", ignoreCase = true) ||
            startsWith("https://", ignoreCase = true) ||
            startsWith("file:", ignoreCase = true) ||
            startsWith("content:", ignoreCase = true)
        ) {
            return this
        }
        return "${BuildConfig.API_BASE_URL.trimEnd('/')}/${trimStart('/')}"
    }

    private fun String.isHlsManifestUrl(): Boolean {
        return substringBefore('?').endsWith(".m3u8", ignoreCase = true)
    }
}
