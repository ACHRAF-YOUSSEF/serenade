package com.serenade.app.feature.player.data

import com.serenade.app.feature.player.PlaybackItem
import com.serenade.app.feature.player.data.entity.PlaybackHistoryEntity
import com.serenade.app.feature.player.data.entity.PlaybackQueueEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackRepository @Inject constructor(
    private val dao: PlaybackDao,
) {
    fun observeQueue(): Flow<List<PlaybackQueueEntity>> = dao.observeQueue()

    fun observeHistory(limit: Int = 50): Flow<List<PlaybackHistoryEntity>> = dao.observeHistory(limit)

    suspend fun savedQueue(): List<PlaybackQueueEntity> = dao.getQueueOnce()

    suspend fun saveQueue(items: List<PlaybackItem>, currentIndex: Int) {
        val now = Instant.now()
        val queue = items.mapIndexed { index, item ->
            PlaybackQueueEntity(
                queuePosition = index,
                trackId = item.trackId,
                streamUrl = item.streamUrl,
                title = item.title,
                artist = item.artist,
                album = item.album,
                durationMs = item.durationMs,
                artworkUrl = item.artworkUrl,
                isCurrent = index == currentIndex,
                positionMs = 0L,
                updatedAt = now,
            )
        }
        dao.replaceQueue(queue)
        queue.getOrNull(currentIndex)?.let { addHistory(it, positionMs = 0L, playedAt = now) }
    }

    suspend fun markCurrent(
        trackId: String,
        queuePosition: Int,
        positionMs: Long,
        addHistory: Boolean,
    ) {
        val now = Instant.now()
        dao.markCurrent(queuePosition, positionMs, now)
        if (addHistory) {
            dao.getQueueItem(trackId)?.let { addHistory(it, positionMs, now) }
        }
    }

    private suspend fun addHistory(
        queueItem: PlaybackQueueEntity,
        positionMs: Long,
        playedAt: Instant,
    ) {
        val existing = dao.getHistory(queueItem.trackId)
        dao.insertHistory(
            PlaybackHistoryEntity(
                trackId = queueItem.trackId,
                title = queueItem.title,
                artist = queueItem.artist,
                album = queueItem.album,
                durationMs = queueItem.durationMs,
                artworkUrl = queueItem.artworkUrl,
                lastPositionMs = positionMs,
                lastPlayedAt = playedAt,
                playCount = (existing?.playCount ?: 0) + 1,
            )
        )
    }
}

fun PlaybackQueueEntity.toPlaybackItem() = PlaybackItem(
    trackId = trackId,
    streamUrl = streamUrl,
    title = title,
    artist = artist,
    album = album,
    durationMs = durationMs,
    artworkUrl = artworkUrl,
)
