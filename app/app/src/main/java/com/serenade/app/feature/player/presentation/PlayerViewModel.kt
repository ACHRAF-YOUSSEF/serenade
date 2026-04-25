package com.serenade.app.feature.player.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenade.app.feature.player.PlaybackState
import com.serenade.app.feature.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val controller: PlayerController,
) : ViewModel() {

    val state: StateFlow<PlaybackState> = controller.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlaybackState())

    init {
        viewModelScope.launch {
            while (isActive) {
                controller.syncPosition()
                delay(500)
            }
        }
    }

    fun togglePlayPause() = controller.togglePlayPause()

    fun seekTo(ms: Long) = controller.seekTo(ms)
}
