package com.serenade.app.feature.playlist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenade.app.feature.playlist.data.PlaylistRepository
import com.serenade.app.feature.playlist.data.remote.dto.PlaylistSummaryResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryFilter { All, Playlists, Albums, Downloaded }

sealed interface LibraryUiState {
    data object Loading : LibraryUiState
    data class Ready(val playlists: List<PlaylistSummaryResponse>, val filter: LibraryFilter) : LibraryUiState
    data class Error(val message: String) : LibraryUiState
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: PlaylistRepository,
) : ViewModel() {

    private val _filter = MutableStateFlow(LibraryFilter.All)
    val filter: StateFlow<LibraryFilter> = _filter

    val state: StateFlow<LibraryUiState> = combine(repository.playlists(), _filter) { playlists, filter ->
        val filtered = when (filter) {
            LibraryFilter.All, LibraryFilter.Playlists -> playlists
            LibraryFilter.Albums -> emptyList()
            LibraryFilter.Downloaded -> emptyList()
        }
        LibraryUiState.Ready(filtered, filter) as LibraryUiState
    }
        .catch { emit(LibraryUiState.Error(it.message ?: "Failed to load playlists")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState.Loading)

    init { load() }

    fun setFilter(filter: LibraryFilter) { _filter.value = filter }

    fun load() {
        viewModelScope.launch {
            runCatching { repository.refresh() }
        }
    }

    fun createPlaylist(name: String, onCreated: (PlaylistSummaryResponse) -> Unit) {
        viewModelScope.launch {
            runCatching { repository.createPlaylist(name) }
                .onSuccess { playlist ->
                    onCreated(playlist)
                    load()
                }
        }
    }
}
