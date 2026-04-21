package com.serenade.app.feature.subtitle.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subtitle_lines")
data class SubtitleLineEntity(
    @PrimaryKey val id: String,
    val trackId: String,
    val startMs: Long,
    val endMs: Long,
    val text: String
)