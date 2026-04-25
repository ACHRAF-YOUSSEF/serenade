package com.serenade.app.feature.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenade.app.feature.track.data.remote.dto.TrackResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val api: SearchApiService,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _genre = MutableStateFlow<String?>(null)
    val genre: StateFlow<String?> = _genre

    val results: StateFlow<List<TrackResponse>> = combine(_query, _genre) { q, g -> q to g }
        .debounce(300)
        .flatMapLatest { (q, g) ->
            flow {
                emit(emptyList())
                if (q.isBlank() && g == null) return@flow
                try {
                    emit(api.search(q = q, genre = g).content)
                } catch (_: Exception) {
                    emit(emptyList())
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQueryChange(q: String) { _query.value = q }

    fun onGenreToggle(g: String) {
        _genre.value = if (_genre.value == g) null else g
    }
}
