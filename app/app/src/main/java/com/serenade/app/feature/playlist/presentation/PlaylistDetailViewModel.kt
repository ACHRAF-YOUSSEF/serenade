package com.serenade.app.feature.playlist.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenade.app.feature.playlist.data.PlaylistRepository
import com.serenade.app.feature.playlist.data.remote.dto.PlaylistDetailResponse
import com.serenade.app.feature.rating.data.RatingRepository
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
    private val playlistRepository: PlaylistRepository,
    private val ratingRepository: RatingRepository,
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
                PlaylistDetailUiState.Ready(playlistRepository.getDetail(playlistId), null)
            } catch (e: Exception) {
                PlaylistDetailUiState.Error(e.message ?: "Failed to load playlist")
            }
        }
    }

    fun rate(value: Int) {
        viewModelScope.launch {
            runCatching {
                ratingRepository.ratePlaylist(playlistId, value)
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
            runCatching { playlistRepository.copyPlaylist(playlistId) }
                .onSuccess { onCopied() }
        }
    }
}
