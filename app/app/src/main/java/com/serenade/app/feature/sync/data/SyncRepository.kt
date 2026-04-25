package com.serenade.app.feature.sync.data

import android.content.Context
import androidx.core.content.edit
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.serenade.app.core.database.Genre
import com.serenade.app.feature.playlist.data.PlaylistDao
import com.serenade.app.feature.playlist.data.entity.PlaylistEntity
import com.serenade.app.feature.rating.data.RatingDao
import com.serenade.app.feature.rating.data.entity.RatingEntity
import com.serenade.app.feature.rating.data.entity.RatingTargetType
import com.serenade.app.feature.sync.data.remote.ChangesApiService
import com.serenade.app.feature.sync.worker.SyncWorker
import com.serenade.app.feature.track.data.TrackDao
import com.serenade.app.feature.track.data.entity.TrackEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val api: ChangesApiService,
    private val trackDao: TrackDao,
    private val playlistDao: PlaylistDao,
    private val ratingDao: RatingDao,
    private val pendingOpDao: PendingOpDao,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun pullChanges() {
        val response = api.getChanges(since = prefs.getString(KEY_CURSOR, INITIAL_CURSOR) ?: INITIAL_CURSOR)

        response.tracks.forEach { track ->
            val existing = trackDao.getByIdOnce(track.id)
            trackDao.insert(
                TrackEntity(
                    id = track.id,
                    remoteId = track.id,
                    title = track.title,
                    artist = track.artist,
                    album = track.album ?: "",
                    genre = runCatching { Genre.valueOf(track.genre) }.getOrDefault(Genre.OTHER),
                    durationMs = track.durationMs ?: 0L,
                    artworkUrl = track.artworkUrl,
                    localPath = existing?.localPath,
                    streamUrl = track.streamUrl,
                    streamUrlExpiresAt = null,
                    isDownloaded = existing?.isDownloaded ?: false,
                    providerId = "serenade",
                    updatedAt = track.updatedAt?.let(Instant::parse) ?: Instant.now(),
                )
            )
        }

        response.playlists.forEach { playlist ->
            playlistDao.insert(
                PlaylistEntity(
                    id = playlist.id,
                    remoteId = playlist.id,
                    name = playlist.name,
                    ownerId = LOCAL_OWNER,
                    isMine = true,
                    isCopy = playlist.isCopy,
                    sourcePlaylistId = playlist.sourcePlaylistId,
                    ratingAvg = playlist.ratingAvg.toFloat(),
                    updatedAt = playlist.updatedAt?.let(Instant::parse) ?: Instant.now(),
                )
            )
        }

        response.ratings.forEach { rating ->
            val targetType = runCatching { RatingTargetType.valueOf(rating.targetType) }
                .getOrDefault(RatingTargetType.UNKNOWN)
            if (targetType != RatingTargetType.UNKNOWN) {
                ratingDao.insert(
                    RatingEntity(
                        id = rating.id,
                        targetType = targetType,
                        targetId = rating.targetId,
                        value = rating.value,
                        syncedAt = rating.updatedAt?.let(Instant::parse) ?: Instant.now(),
                    )
                )
            }
        }

        prefs.edit { putString(KEY_CURSOR, response.nextCursor) }
        flushPendingOpsPlaceholder()
    }

    fun schedulePeriodicSync() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    private suspend fun flushPendingOpsPlaceholder() {
        pendingOpDao.getPendingList()
        // Repositories do not yet write outbox mutations. Keep hook here for M9 flush work.
    }

    private companion object {
        const val PREFS_NAME = "serenade_sync"
        const val KEY_CURSOR = "changes_cursor"
        const val INITIAL_CURSOR = "1970-01-01T00:00:00Z"
        const val LOCAL_OWNER = "me"
    }
}
