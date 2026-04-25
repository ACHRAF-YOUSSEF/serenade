package com.serenade.app.feature.player

import android.content.ContentValues.TAG
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton
import androidx.media3.common.MimeTypes
import com.serenade.app.BuildConfig

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentTrackId: String? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
)

@Singleton
class PlayerController @Inject constructor(
    val player: ExoPlayer,
) {
    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.value = _state.value.copy(isPlaying = isPlaying)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _state.value = _state.value.copy(
                    currentTrackId = mediaItem?.mediaId,
                    positionMs = 0L,
                    durationMs = player.duration.coerceAtLeast(0L),
                )
            }
        })
    }

    fun play(trackId: String, streamUrl: String) {
        val baseUrl = BuildConfig.API_BASE_URL
        
        val fullUrl = if (streamUrl.startsWith("http")) streamUrl else baseUrl + streamUrl

        val item = MediaItem.Builder()
            .setMediaId(trackId)
            .setUri(fullUrl)
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .build()

        Log.i(TAG, "play: Playing trackId=$trackId, streamUrl=$streamUrl")
        Log.i(TAG, "play: item=$item")

        player.setMediaItem(item)
        player.prepare()
        player.play()
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    fun stop() {
        player.stop()
        _state.value = PlaybackState()
    }

    fun syncPosition() {
        if (_state.value.currentTrackId == null) return
        _state.value = _state.value.copy(
            positionMs = player.currentPosition,
            durationMs = player.duration.coerceAtLeast(0L),
        )
    }

    fun release() {
        player.release()
    }
}
