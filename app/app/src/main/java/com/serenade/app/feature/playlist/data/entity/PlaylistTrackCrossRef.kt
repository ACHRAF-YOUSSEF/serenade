package com.serenade.app.feature.playlist.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.serenade.app.feature.track.data.entity.TrackEntity

@Entity(
    tableName = "playlist_track_cross_refs",
    primaryKeys = ["playlistId", "trackId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("trackId")]
)
data class PlaylistTrackCrossRef(
    val playlistId: String,
    val trackId: String,
    val position: Int
)
