package com.serenade.app.feature.player.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "playback_history",
    indices = [Index("lastPlayedAt")],
)
data class PlaybackHistoryEntity(
    @PrimaryKey val trackId: String,
    val title: String?,
    val artist: String?,
    val album: String?,
    val durationMs: Long,
    val artworkUrl: String?,
    val lastPositionMs: Long,
    val lastPlayedAt: Instant,
    val playCount: Int,
)
