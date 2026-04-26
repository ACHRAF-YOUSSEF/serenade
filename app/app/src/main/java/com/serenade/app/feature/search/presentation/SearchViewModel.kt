package com.serenade.app.feature.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenade.app.core.database.Genre
import com.serenade.app.feature.track.data.remote.dto.TrackResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val api: SearchApiService,
) : ViewModel() {

    private val allGenres: Set<String> = Genre.entries.map { it.name }.toSet()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _genres = MutableStateFlow(allGenres)
    val genres: StateFlow<Set<String>> = _genres

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    @OptIn(ExperimentalCoroutinesApi::class)
    val results: StateFlow<List<TrackResponse>> = combine(_query, _genres) { q, g -> q to g }
        .debounce(300)
        .flatMapLatest { (q, g) ->
            flow {
                emit(emptyList())
                val hasGenreFilter = g != allGenres
                if (q.isBlank() && !hasGenreFilter) {
                    _error.value = null
                    return@flow
                }
                if (g.isEmpty()) {
                    _error.value = null
                    return@flow
                }
                try {
                    _error.value = null
                    val genresParam = if (!hasGenreFilter) null else g.joinToString(",")
                    emit(api.search(q = q, genres = genresParam).content)
                } catch (e: Exception) {
                    _error.value = e.message ?: "Search failed"
                    emit(emptyList())
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQueryChange(q: String) { _query.value = q }

    fun onGenreToggle(g: String) {
        _genres.update { current -> if (g in current) current - g else current + g }
    }

    fun onSelectAllGenres() {
        _genres.value = allGenres
    }
}
