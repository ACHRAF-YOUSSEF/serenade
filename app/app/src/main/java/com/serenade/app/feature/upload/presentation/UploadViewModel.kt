package com.serenade.app.feature.upload.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenade.app.core.database.Genre
import com.serenade.app.feature.auth.data.AuthRepository
import com.serenade.app.feature.track.data.TrackSyncRepository
import com.serenade.app.feature.upload.data.UploadFileInfo
import com.serenade.app.feature.upload.data.UploadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UploadUiState(
    val selectedFile: UploadFileInfo? = null,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val genre: Genre = Genre.OTHER,
    val isUploading: Boolean = false,
    val isPolling: Boolean = false,
    val progressPercent: Int = 0,
    val trackId: String? = null,
    val status: String? = null,
    val error: String? = null,
) {
    val canUpload: Boolean
        get() = selectedFile != null &&
                title.isNotBlank() &&
                artist.isNotBlank() &&
                !isUploading &&
                !isPolling
}

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val uploadRepository: UploadRepository,
    private val trackSyncRepository: TrackSyncRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UploadUiState())
    val state: StateFlow<UploadUiState> = _state

    private var pollJob: Job? = null

    fun selectFile(uri: Uri) {
        runCatching { uploadRepository.inspect(uri) }
            .onSuccess { info ->
                _state.update { current ->
                    val inferredTitle = info.name.substringBeforeLast('.').takeIf { it.isNotBlank() }.orEmpty()
                    current.copy(
                        selectedFile = info,
                        title = current.title.ifBlank { inferredTitle },
                        error = null,
                    )
                }
            }
            .onFailure { error ->
                _state.update { it.copy(error = error.message ?: "Unable to read selected file") }
            }
    }

    fun updateTitle(value: String) {
        _state.update { it.copy(title = value, error = null) }
    }

    fun updateArtist(value: String) {
        _state.update { it.copy(artist = value, error = null) }
    }

    fun updateAlbum(value: String) {
        _state.update { it.copy(album = value, error = null) }
    }

    fun updateGenre(value: Genre) {
        _state.update { it.copy(genre = value, error = null) }
    }

    fun upload() {
        val snapshot = _state.value
        val file = snapshot.selectedFile ?: return
        if (!authRepository.isLoggedIn()) {
            _state.update { it.copy(error = "Sign in required") }
            return
        }

        if (!uploadRepository.isNetworkAvailable()) {
            viewModelScope.launch {
                runCatching {
                    uploadRepository.queueUpload(
                        uri = file.uri,
                        title = snapshot.title,
                        artist = snapshot.artist,
                        album = snapshot.album,
                        genre = snapshot.genre,
                    )
                }.onSuccess {
                    _state.update { it.copy(status = STATUS_QUEUED, error = null) }
                }.onFailure { err ->
                    _state.update { it.copy(error = err.message ?: "Failed to queue upload") }
                }
            }
            return
        }

        viewModelScope.launch {
            pollJob?.cancel()
            _state.update {
                it.copy(
                    isUploading = true,
                    isPolling = false,
                    progressPercent = 0,
                    trackId = null,
                    status = null,
                    error = null,
                )
            }
            runCatching {
                uploadRepository.upload(
                    uri = file.uri,
                    title = snapshot.title,
                    artist = snapshot.artist,
                    album = snapshot.album,
                    genre = snapshot.genre,
                    onProgress = { bytes, total ->
                        val percent = if (total > 0L) ((bytes * 100L) / total).toInt() else 0
                        _state.update { it.copy(progressPercent = percent.coerceIn(0, 100)) }
                    },
                )
            }.onSuccess { response ->
                _state.update {
                    it.copy(
                        isUploading = false,
                        progressPercent = 100,
                        trackId = response.trackId,
                        status = response.status,
                    )
                }
                startPolling(response.trackId)
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isUploading = false,
                        error = error.message ?: "Upload failed",
                    )
                }
            }
        }
    }

    private fun startPolling(trackId: String) {
        pollJob = viewModelScope.launch {
            _state.update { it.copy(isPolling = true, error = null) }
            repeat(MAX_STATUS_POLLS) {
                val status = runCatching { uploadRepository.status(trackId) }
                    .onFailure { error ->
                        _state.update {
                            it.copy(
                                isPolling = false,
                                error = error.message ?: "Status check failed",
                            )
                        }
                    }
                    .getOrNull() ?: return@launch

                _state.update {
                    it.copy(
                        status = status.status,
                        trackId = status.trackId,
                        isPolling = status.status == STATUS_PROCESSING,
                    )
                }

                when (status.status) {
                    STATUS_READY -> {
                        runCatching { trackSyncRepository.sync() }
                        return@launch
                    }
                    STATUS_FAILED -> return@launch
                    else -> delay(STATUS_POLL_DELAY_MS)
                }
            }
            _state.update { it.copy(isPolling = false) }
        }
    }

    override fun onCleared() {
        pollJob?.cancel()
        super.onCleared()
    }

    private companion object {
        const val STATUS_PROCESSING = "PROCESSING"
        const val STATUS_READY = "READY"
        const val STATUS_FAILED = "FAILED"
        const val STATUS_QUEUED = "QUEUED"
        const val STATUS_POLL_DELAY_MS = 2_500L
        const val MAX_STATUS_POLLS = 120
    }
}
