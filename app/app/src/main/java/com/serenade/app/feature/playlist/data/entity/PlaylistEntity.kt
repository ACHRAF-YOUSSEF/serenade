package com.serenade.app.feature.playlist.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val remoteId: String?,
    val name: String,
    val ownerId: String,
    val isMine: Boolean,
    val isCopy: Boolean,
    val sourcePlaylistId: String?,
    val ratingAvg: Float,
    val updatedAt: Instant
)