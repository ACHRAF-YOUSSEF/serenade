package com.serenade.app.feature.playlist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenade.app.feature.playlist.data.remote.PlaylistApiService
import com.serenade.app.feature.playlist.data.remote.dto.CreatePlaylistRequest
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
    private val api: PlaylistApiService,
) : ViewModel() {

    private val _state = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val state: StateFlow<LibraryUiState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = LibraryUiState.Loading
            _state.value = try {
                LibraryUiState.Ready(api.getMyPlaylists().content)
            } catch (e: Exception) {
                LibraryUiState.Error(e.message ?: "Failed to load playlists")
            }
        }
    }

    fun createPlaylist(name: String, onCreated: (PlaylistSummaryResponse) -> Unit) {
        viewModelScope.launch {
            runCatching { api.createPlaylist(CreatePlaylistRequest(name)) }
                .onSuccess { playlist ->
                    onCreated(playlist)
                    load()
                }
        }
    }
}
