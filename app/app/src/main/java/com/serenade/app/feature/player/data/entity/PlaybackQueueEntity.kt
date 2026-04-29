package com.serenade.app.feature.player.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "playback_queue",
    indices = [
        Index("trackId"),
        Index("isCurrent"),
    ],
)
data class PlaybackQueueEntity(
    @PrimaryKey val queuePosition: Int,
    val trackId: String,
    val streamUrl: String,
    val title: String?,
    val artist: String?,
    val album: String?,
    val durationMs: Long,
    val artworkUrl: String?,
    val isCurrent: Boolean,
    val positionMs: Long,
    val updatedAt: Instant,
)
