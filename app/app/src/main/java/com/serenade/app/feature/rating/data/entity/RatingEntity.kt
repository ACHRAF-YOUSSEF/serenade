package com.serenade.app.feature.rating.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "ratings")
data class RatingEntity(
    @PrimaryKey val id: String,
    val targetType: RatingTargetType,
    val targetId: String,
    val value: Int,
    val syncedAt: Instant?
)