package com.serenade.app.feature.player

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

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
        val item = MediaItem.Builder()
            .setMediaId(trackId)
            .setUri(streamUrl)
            .build()
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
