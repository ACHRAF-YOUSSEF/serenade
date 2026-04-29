package com.serenade.app.feature.rating.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "ratings",
    indices = [Index(value = ["targetType", "targetId"])]
)
data class RatingEntity(
    @PrimaryKey val id: String,
    val targetType: RatingTargetType,
    val targetId: String,
    val value: Int,
    val syncedAt: Instant?
) {
    init {
        require(value in MIN_VALUE..MAX_VALUE) {
            "Rating value must be between $MIN_VALUE and $MAX_VALUE."
        }
    }

    private companion object {
        const val MIN_VALUE = 1
        const val MAX_VALUE = 5
    }
}
