package com.serenade.app.feature.providers.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey val id: String,
    val manifestUrl: String,
    val name: String,
    val version: String,
    val capabilities: List<String>,
    val enabled: Boolean
)
