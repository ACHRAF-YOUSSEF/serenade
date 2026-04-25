package com.serenade.app.feature.track.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenade.app.feature.download.data.DownloadRepository
import com.serenade.app.feature.download.data.entity.DownloadEntity
import com.serenade.app.feature.sync.data.SyncRepository
import com.serenade.app.feature.track.data.TrackSyncRepository
import com.serenade.app.feature.track.data.entity.TrackEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TrackListUiState {
    data object Loading : TrackListUiState
    data class Ready(val tracks: List<TrackEntity>) : TrackListUiState
    data class Error(val message: String) : TrackListUiState
}

@HiltViewModel
class TrackListViewModel @Inject constructor(
    private val repo: TrackSyncRepository,
    private val downloadRepository: DownloadRepository,
    private val syncRepository: SyncRepository,
) : ViewModel() {

    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing

    val downloadsByTrackId: StateFlow<Map<String, DownloadEntity>> = downloadRepository.downloads()
        .map { downloads -> downloads.associateBy { it.trackId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val state: StateFlow<TrackListUiState> = repo.tracks()
        .map<List<TrackEntity>, TrackListUiState> { TrackListUiState.Ready(it) }
        .catch { emit(TrackListUiState.Error(it.message ?: "Unknown error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TrackListUiState.Loading)

    init {
        syncRepository.schedulePeriodicSync()
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _syncing.value = true
            runCatching {
                syncRepository.pullChanges()
                repo.sync()
            }
            _syncing.value = false
        }
    }

    fun queueDownload(track: TrackEntity) {
        viewModelScope.launch {
            downloadRepository.queueDownload(track)
        }
    }

    fun deleteDownload(trackId: String) {
        viewModelScope.launch {
            downloadRepository.deleteDownload(trackId)
        }
    }
}
