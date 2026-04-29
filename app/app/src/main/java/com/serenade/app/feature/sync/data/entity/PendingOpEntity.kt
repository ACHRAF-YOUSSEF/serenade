package com.serenade.app.feature.sync.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "pending_ops")
data class PendingOpEntity(
    @PrimaryKey val id: String,
    val type: PendingOpType,
    val payloadJson: String,
    val createdAt: Instant
)
