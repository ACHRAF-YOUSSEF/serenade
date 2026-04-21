package com.serenade.app.feature.track.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.serenade.app.core.database.Genre
import java.time.Instant

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val remoteId: String?,
    val title: String,
    val artist: String,
    val album: String,
    val genre: Genre,
    val durationMs: Long,
    val artworkUrl: String?,
    val localPath: String?,
    val streamUrl: String?,
    val isDownloaded: Boolean,
    val providerId: String,
    val updatedAt: Instant
)