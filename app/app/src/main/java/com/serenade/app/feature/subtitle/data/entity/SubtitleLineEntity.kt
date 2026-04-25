package com.serenade.app.feature.subtitle.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.serenade.app.feature.track.data.entity.TrackEntity

@Entity(
    tableName = "subtitle_lines",
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
data class SubtitleLineEntity(
    @PrimaryKey val id: String,
    val trackId: String,
    val startMs: Long,
    val endMs: Long,
    val text: String
)
