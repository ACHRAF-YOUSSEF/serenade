package com.serenade.app.feature.playlist.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenade.app.feature.playlist.data.remote.PlaylistApiService
import com.serenade.app.feature.playlist.data.remote.dto.PlaylistDetailResponse
import com.serenade.app.feature.rating.data.remote.RatingApiService
import com.serenade.app.feature.rating.data.remote.dto.RatingRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PlaylistDetailUiState {
    data object Loading : PlaylistDetailUiState
    data class Ready(val detail: PlaylistDetailResponse, val myRating: Int?) : PlaylistDetailUiState
    data class Error(val message: String) : PlaylistDetailUiState
}

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val playlistApi: PlaylistApiService,
    private val ratingApi: RatingApiService,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val playlistId: String = checkNotNull(savedStateHandle["playlistId"])

    private val _state = MutableStateFlow<PlaylistDetailUiState>(PlaylistDetailUiState.Loading)
    val state: StateFlow<PlaylistDetailUiState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = PlaylistDetailUiState.Loading
            _state.value = try {
                PlaylistDetailUiState.Ready(playlistApi.getPlaylist(playlistId), null)
            } catch (e: Exception) {
                PlaylistDetailUiState.Error(e.message ?: "Failed to load playlist")
            }
        }
    }

    fun rate(value: Int) {
        viewModelScope.launch {
            runCatching {
                ratingApi.rate(RatingRequest("PLAYLIST", playlistId, value))
            }.onSuccess {
                val current = _state.value
                if (current is PlaylistDetailUiState.Ready) {
                    _state.value = current.copy(myRating = value)
                }
            }
        }
    }

    fun copyPlaylist(onCopied: () -> Unit) {
        viewModelScope.launch {
            runCatching { playlistApi.copyPlaylist(playlistId) }
                .onSuccess { onCopied() }
        }
    }
}
