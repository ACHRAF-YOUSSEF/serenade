package com.serenade.app.feature.player.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenade.app.feature.player.PlaybackState
import com.serenade.app.feature.player.PlayerController
import com.serenade.app.feature.subtitle.data.SubtitleRepository
import com.serenade.app.feature.subtitle.data.entity.SubtitleLineEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val controller: PlayerController,
    private val subtitleRepository: SubtitleRepository,
) : ViewModel() {

    val state: StateFlow<PlaybackState> = controller.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlaybackState())

    private val _subtitleLines = MutableStateFlow<List<SubtitleLineEntity>>(emptyList())

    val currentCue: StateFlow<String?> = combine(state, _subtitleLines) { s, lines ->
        lines.firstOrNull { s.positionMs >= it.startMs && s.positionMs < it.endMs }?.text
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            while (isActive) {
                controller.syncPosition()
                delay(500)
            }
        }
        viewModelScope.launch {
            state.map { it.currentTrackId }
                .distinctUntilChanged()
                .collect { trackId ->
                    _subtitleLines.value = if (trackId != null) {
                        subtitleRepository.getSubtitles(trackId)
                    } else {
                        emptyList()
                    }
                }
        }
    }

    fun togglePlayPause() = controller.togglePlayPause()

    fun seekTo(ms: Long) = controller.seekTo(ms)
}
