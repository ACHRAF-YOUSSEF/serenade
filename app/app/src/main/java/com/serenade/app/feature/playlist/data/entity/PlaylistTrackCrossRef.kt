package com.serenade.app.feature.playlist.data.entity

import androidx.room.Entity

@Entity(
    tableName = "playlist_track_cross_refs",
    primaryKeys = ["playlistId", "trackId"]
)
data class PlaylistTrackCrossRef(
    val playlistId: String,
    val trackId: String,
    val position: Int
)