package com.serenade.app.feature.track.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
) : ViewModel() {

    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing

    val state: StateFlow<TrackListUiState> = repo.tracks()
        .map<List<TrackEntity>, TrackListUiState> { TrackListUiState.Ready(it) }
        .catch { emit(TrackListUiState.Error(it.message ?: "Unknown error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TrackListUiState.Loading)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _syncing.value = true
            runCatching { repo.sync() }
            _syncing.value = false
        }
    }
}
