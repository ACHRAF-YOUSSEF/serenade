package com.serenade.app.feature.playlist.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenade.app.feature.playlist.data.PlaylistRepository
import com.serenade.app.feature.playlist.data.remote.dto.PlaylistDetailResponse
import com.serenade.app.feature.track.data.remote.dto.TrackResponse
import com.serenade.app.feature.rating.data.RatingDao
import com.serenade.app.feature.rating.data.RatingRepository
import com.serenade.app.feature.rating.data.entity.RatingTargetType
import com.serenade.app.feature.track.data.TrackDao
import com.serenade.app.feature.track.data.entity.TrackEntity
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
    private val trackDao: TrackDao,
    private val ratingDao: RatingDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val playlistId: String = checkNotNull(savedStateHandle["playlistId"])

    private val _state = MutableStateFlow<PlaylistDetailUiState>(PlaylistDetailUiState.Loading)
    val state: StateFlow<PlaylistDetailUiState> = _state

    val allTracks: StateFlow<List<TrackEntity>> = trackDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = PlaylistDetailUiState.Loading
            _state.value = try {
                val detail = playlistRepository.getDetail(playlistId)
                val myRating =
                    ratingDao.getByTargetOnce(RatingTargetType.PLAYLIST, playlistId)?.value
                PlaylistDetailUiState.Ready(detail, myRating)
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

    fun addTrack(trackId: String) {
        viewModelScope.launch {
            runCatching { playlistRepository.addTrack(playlistId, trackId) }
            load()
        }
    }

    fun removeTrack(trackId: String) {
        viewModelScope.launch {
            runCatching { playlistRepository.removeTrack(playlistId, trackId) }
            load()
        }
    }

    fun moveTrackUp(trackId: String) {
        val s = _state.value as? PlaylistDetailUiState.Ready ?: return
        val tracks = s.detail.tracks.toMutableList()
        val idx = tracks.indexOfFirst { it.id == trackId }
        if (idx <= 0) return
        tracks.add(idx - 1, tracks.removeAt(idx))
        applyReorder(s, tracks)
    }

    fun moveTrackDown(trackId: String) {
        val s = _state.value as? PlaylistDetailUiState.Ready ?: return
        val tracks = s.detail.tracks.toMutableList()
        val idx = tracks.indexOfFirst { it.id == trackId }
        if (idx < 0 || idx >= tracks.size - 1) return
        tracks.add(idx + 1, tracks.removeAt(idx))
        applyReorder(s, tracks)
    }

    private fun applyReorder(s: PlaylistDetailUiState.Ready, newOrder: MutableList<TrackResponse>) {
        _state.value = s.copy(detail = s.detail.copy(tracks = newOrder))
        viewModelScope.launch {
            runCatching {
                playlistRepository.reorderTracks(playlistId, newOrder.map { it.id })
            }
        }
    }
}
