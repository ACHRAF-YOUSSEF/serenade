package com.serenade.app.feature.player.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.serenade.app.feature.player.data.entity.PlaybackHistoryEntity
import com.serenade.app.feature.player.data.entity.PlaybackQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PlaybackDao {
    @Query("SELECT * FROM playback_queue ORDER BY queuePosition ASC")
    abstract fun observeQueue(): Flow<List<PlaybackQueueEntity>>

    @Query("SELECT * FROM playback_queue ORDER BY queuePosition ASC")
    abstract suspend fun getQueueOnce(): List<PlaybackQueueEntity>

    @Query("SELECT * FROM playback_queue WHERE trackId = :trackId LIMIT 1")
    abstract suspend fun getQueueItem(trackId: String): PlaybackQueueEntity?

    @Query("SELECT * FROM playback_history ORDER BY lastPlayedAt DESC LIMIT :limit")
    abstract fun observeHistory(limit: Int): Flow<List<PlaybackHistoryEntity>>

    @Query("SELECT * FROM playback_history WHERE trackId = :trackId")
    abstract suspend fun getHistory(trackId: String): PlaybackHistoryEntity?

    @Query("DELETE FROM playback_queue")
    abstract suspend fun clearQueue()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertQueue(items: List<PlaybackQueueEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertHistory(item: PlaybackHistoryEntity)

    @Query("UPDATE playback_queue SET isCurrent = 0")
    abstract suspend fun clearCurrent()

    @Query(
        "UPDATE playback_queue SET isCurrent = 1, positionMs = :positionMs, updatedAt = :updatedAt " +
            "WHERE queuePosition = :queuePosition"
    )
    abstract suspend fun updateCurrent(queuePosition: Int, positionMs: Long, updatedAt: java.time.Instant)

    @Transaction
    open suspend fun replaceQueue(items: List<PlaybackQueueEntity>) {
        clearQueue()
        insertQueue(items)
    }

    @Transaction
    open suspend fun markCurrent(
        queuePosition: Int,
        positionMs: Long,
        updatedAt: java.time.Instant,
    ) {
        clearCurrent()
        updateCurrent(queuePosition, positionMs, updatedAt)
    }
}
