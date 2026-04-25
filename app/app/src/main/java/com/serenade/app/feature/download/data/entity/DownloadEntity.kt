package com.serenade.app.feature.download.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.serenade.app.feature.track.data.entity.TrackEntity

@Entity(
    tableName = "downloads",
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("trackId")]
)
data class DownloadEntity(
    @PrimaryKey val id: String,
    val trackId: String,
    val state: DownloadState,
    val progress: Int,
    val filePath: String?
)
