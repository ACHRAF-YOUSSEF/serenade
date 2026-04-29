package com.serenade.app.feature.search.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.serenade.app.core.database.Genre
import com.serenade.app.feature.track.data.remote.dto.TrackResponse
import com.serenade.app.ui.design.ArtworkAvatar
import com.serenade.app.ui.design.SrChip
import com.serenade.app.ui.design.SrEyebrow
import com.serenade.app.ui.design.SrScreenBackground
import com.serenade.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onTrackClick: (TrackResponse, List<TrackResponse>) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel,
    isOnline: Boolean = true,
) {
    val query by viewModel.query.collectAsState()
    val selectedGenres by viewModel.genres.collectAsState()
    val results by viewModel.results.collectAsState()
    val error by viewModel.error.collectAsState()
    val loading by viewModel.loading.collectAsState()

    Scaffold(
        containerColor = SrBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Search",
                        style = MaterialTheme.typography.displaySmall,
                        color = SrText,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SrText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SrBg),
            )
        }
    ) { padding ->
        SrScreenBackground(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                SearchBox(
                    query = query,
                    onQueryChange = viewModel::onQueryChange,
                    onClear = { viewModel.onQueryChange("") },
                )
                SrEyebrow(
                    text = "Browse by genre",
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                )
                FlowRow(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Genre.entries.forEach { g ->
                        val selected = g.name in selectedGenres
                        SrChip(
                            label = g.name,
                            selected = selected,
                            onClick = { viewModel.onGenreToggle(g.name) },
                        )
                    }
                }

                when {
                    !isOnline -> Box(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Search unavailable offline",
                            color = SrTextDim,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    error != null -> Box(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = error ?: "",
                            color = SrCoral,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    loading -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = SrPrimary)
                    }
                    results.isEmpty() && !query.isBlank() -> Box(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No results for \"$query\"",
                            color = SrTextDim,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 18.dp, bottom = 20.dp),
                    ) {
                        items(results, key = { it.id }) { track ->
                            SearchTrackRow(
                                track = track,
                                onClick = { onTrackClick(track, results) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBox(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SrSurface.copy(alpha = 0.9f))
            .padding(horizontal = 14.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = SrTextMute, modifier = Modifier.size(20.dp))
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search tracks, artists, playlists") },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SrSurface.copy(alpha = 0.9f),
                unfocusedContainerColor = SrSurface.copy(alpha = 0.9f),
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                focusedTextColor = SrText,
                unfocusedTextColor = SrText,
                focusedPlaceholderColor = SrTextMute,
                unfocusedPlaceholderColor = SrTextMute,
            ),
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search", tint = SrTextMute)
                    }
                }
            },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SearchTrackRow(track: TrackResponse, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.size(46.dp).clip(RoundedCornerShape(8.dp))) {
            ArtworkAvatar(seed = track.title, size = 46.dp, cornerRadius = 8.dp)
            if (!track.artworkUrl.isNullOrBlank()) {
                AsyncImage(
                    model = track.artworkUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(track.title, style = MaterialTheme.typography.titleSmall, color = SrText, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(track.artist, style = MaterialTheme.typography.bodySmall, color = SrTextDim, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        track.genre.takeIf { it.isNotBlank() }?.let {
            Text(it, style = MaterialTheme.typography.labelSmall, color = SrTextMute)
        }
    }
}
