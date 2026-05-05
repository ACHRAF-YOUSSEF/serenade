package com.serenade.app.feature.download.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.serenade.app.feature.track.data.entity.TrackEntity
import com.serenade.app.ui.design.ArtworkAvatar
import com.serenade.app.ui.design.SrEyebrow
import com.serenade.app.ui.design.SrScreenBackground
import com.serenade.app.ui.design.SrSurfaceCard
import com.serenade.app.ui.theme.SrBg
import com.serenade.app.ui.theme.SrCoral
import com.serenade.app.ui.theme.SrLineHi
import com.serenade.app.ui.theme.SrPrimary
import com.serenade.app.ui.theme.SrSurface
import com.serenade.app.ui.theme.SrSurfaceHi
import com.serenade.app.ui.theme.SrText
import com.serenade.app.ui.theme.SrTextDim
import com.serenade.app.ui.theme.SrTextMute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadScreen(
    onTrackClick: (TrackEntity) -> Unit,
    onBack: () -> Unit,
    viewModel: DownloadViewModel,
) {
    val tracks by viewModel.downloadedTracks.collectAsState()

    Scaffold(
        containerColor = SrBg,
        topBar = {
            TopAppBar(
                title = { Text("Downloads", style = MaterialTheme.typography.displaySmall, color = SrText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SrText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SrBg),
            )
        },
    ) { padding ->
        SrScreenBackground(modifier = Modifier.padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    DownloadsHeader(trackCount = tracks.size)
                }
                item {
                    StorageCard(trackCount = tracks.size)
                }
                if (tracks.isEmpty()) {
                    item {
                        EmptyDownloadsCard()
                    }
                } else {
                    item {
                        SrEyebrow("Ready offline")
                    }
                    items(tracks, key = { it.id }) { track ->
                        DownloadTrackRow(
                            track = track,
                            onClick = { onTrackClick(track) },
                            onDelete = { viewModel.deleteDownload(track.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadsHeader(trackCount: Int) {
    Column {
        Text(
            text = "Offline listening.",
            style = MaterialTheme.typography.displaySmall,
            color = SrText,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "$trackCount tracks ready on this device",
            style = MaterialTheme.typography.bodySmall,
            color = SrTextDim,
        )
    }
}

@Composable
private fun StorageCard(trackCount: Int) {
    val used = (0.18f + trackCount.coerceAtMost(20) * 0.025f).coerceAtMost(0.72f)
    SrSurfaceCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SrSurfaceHi),
            ) {
                Icon(Icons.Default.DownloadDone, contentDescription = null, tint = SrPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Device storage", style = MaterialTheme.typography.titleSmall, color = SrText)
                Text("Serenade cache estimate", style = MaterialTheme.typography.bodySmall, color = SrTextDim)
            }
            Text("${trackCount}", style = MaterialTheme.typography.titleMedium, color = SrPrimary)
        }
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SrSurfaceHi),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(used)
                    .background(Brush.horizontalGradient(listOf(SrPrimary, SrCoral))),
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("LOCAL CACHE", style = MaterialTheme.typography.labelSmall, color = SrTextMute)
            Text("APP PRIVATE", style = MaterialTheme.typography.labelSmall, color = SrTextMute)
        }
    }
}

@Composable
private fun EmptyDownloadsCard() {
    SrSurfaceCard(modifier = Modifier.fillMaxWidth()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(SrSurfaceHi),
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = SrPrimary)
                }
                Spacer(Modifier.height(12.dp))
                Text("No downloads yet", style = MaterialTheme.typography.titleMedium, color = SrText)
                Text("Use the download action on tracks.", style = MaterialTheme.typography.bodySmall, color = SrTextDim)
            }
        }
    }
}

@Composable
private fun DownloadTrackRow(
    track: TrackEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SrSurface.copy(alpha = 0.58f))
            .border(1.dp, SrLineHi, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ArtworkAvatar(seed = track.title, size = 46.dp, cornerRadius = 8.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleSmall,
                color = SrText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = listOf(track.artist, formatDuration(track.durationMs)).filter { it.isNotBlank() }.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = SrTextDim,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(Icons.Default.DownloadDone, contentDescription = "Downloaded", tint = SrPrimary, modifier = Modifier.size(18.dp))
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete download", tint = SrTextMute)
        }
    }
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0L) return ""
    val totalSec = ms / 1000
    return "%d:%02d".format(totalSec / 60, totalSec % 60)
}
