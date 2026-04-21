package com.serenade.app.feature.download.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String,
    val trackId: String,
    val state: DownloadState,
    val progress: Int,
    val filePath: String?
)
