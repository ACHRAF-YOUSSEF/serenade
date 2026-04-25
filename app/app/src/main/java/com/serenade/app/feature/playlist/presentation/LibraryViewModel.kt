package com.serenade.app.feature.playlist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenade.app.feature.playlist.data.PlaylistRepository
import com.serenade.app.feature.playlist.data.remote.dto.PlaylistSummaryResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LibraryUiState {
    data object Loading : LibraryUiState
    data class Ready(val playlists: List<PlaylistSummaryResponse>) : LibraryUiState
    data class Error(val message: String) : LibraryUiState
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: PlaylistRepository,
) : ViewModel() {

    val state: StateFlow<LibraryUiState> = repository.playlists()
        .map<List<PlaylistSummaryResponse>, LibraryUiState> { LibraryUiState.Ready(it) }
        .catch { emit(LibraryUiState.Error(it.message ?: "Failed to load playlists")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState.Loading)

    init { load() }

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
