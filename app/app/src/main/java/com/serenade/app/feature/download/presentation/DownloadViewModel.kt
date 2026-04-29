package com.serenade.app.feature.download.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenade.app.feature.download.data.DownloadRepository
import com.serenade.app.feature.player.PlayerController
import com.serenade.app.feature.track.data.entity.TrackEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val repository: DownloadRepository,
    private val playerController: PlayerController,
) : ViewModel() {
    val downloadedTracks: StateFlow<List<TrackEntity>> = repository.downloadedTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteDownload(trackId: String) {
        if (playerController.state.value.currentTrackId == trackId) {
            playerController.stopPlayback(clearPersistedQueue = true)
        }
        viewModelScope.launch {
            repository.deleteDownload(trackId)
        }
    }
}
