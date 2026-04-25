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
import com.serenade.app.feature.playlist.data.remote.PlaylistApiService
import com.serenade.app.feature.playlist.data.remote.dto.CreatePlaylistRequest
import com.serenade.app.feature.playlist.data.remote.dto.PlaylistSummaryResponse
import com.serenade.app.feature.rating.data.RatingDao
import com.serenade.app.feature.rating.data.entity.RatingEntity
import com.serenade.app.feature.rating.data.entity.RatingTargetType
import com.serenade.app.feature.rating.data.remote.RatingApiService
import com.serenade.app.feature.playlist.data.remote.dto.TrackPositionRequest
import com.serenade.app.feature.rating.data.remote.dto.RatingRequest
import com.serenade.app.feature.rating.data.remote.dto.RatingResponse
import com.serenade.app.feature.sync.data.entity.AddPlaylistTrackOpPayload
import com.serenade.app.feature.sync.data.entity.CopyPlaylistOpPayload
import com.serenade.app.feature.sync.data.entity.CreatePlaylistOpPayload
import com.serenade.app.feature.sync.data.entity.PendingOpEntity
import com.serenade.app.feature.sync.data.entity.PendingOpJson
import com.serenade.app.feature.sync.data.entity.PendingOpType
import com.serenade.app.feature.sync.data.entity.RateOpPayload
import com.serenade.app.feature.sync.data.entity.RemovePlaylistTrackOpPayload
import com.serenade.app.feature.sync.data.entity.ReorderPlaylistTracksOpPayload
import com.serenade.app.feature.sync.data.entity.UploadTrackOpPayload
import com.serenade.app.feature.upload.data.remote.UploadApiService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
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
    private val playlistApi: PlaylistApiService,
    private val ratingApi: RatingApiService,
    private val uploadApi: UploadApiService,
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
        flushPendingOps()
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

    private suspend fun flushPendingOps() {
        for (op in pendingOpDao.getPendingList()) {
            val applied = runCatching { applyPendingOp(op) }.isSuccess
            if (!applied) return
            pendingOpDao.deleteById(op.id)
        }
    }

    private suspend fun applyPendingOp(op: PendingOpEntity) {
        when (op.type) {
            PendingOpType.CREATE_PLAYLIST -> {
                val payload = PendingOpJson.decodeFromString<CreatePlaylistOpPayload>(op.payloadJson)
                val playlist = playlistApi.createPlaylist(CreatePlaylistRequest(payload.name))
                playlistDao.insert(playlist.toEntity())
                playlistDao.deleteById(payload.localId)
            }
            PendingOpType.COPY_PLAYLIST -> {
                val payload = PendingOpJson.decodeFromString<CopyPlaylistOpPayload>(op.payloadJson)
                playlistDao.insert(playlistApi.copyPlaylist(payload.sourcePlaylistId).toEntity())
            }
            PendingOpType.RATE_PLAYLIST -> {
                val payload = PendingOpJson.decodeFromString<RateOpPayload>(op.payloadJson)
                ratingDao.insert(
                    ratingApi.rate(RatingRequest("PLAYLIST", payload.targetId, payload.value))
                        .toEntity(RatingTargetType.PLAYLIST)
                )
            }
            PendingOpType.RATE_TRACK -> {
                val payload = PendingOpJson.decodeFromString<RateOpPayload>(op.payloadJson)
                ratingDao.insert(
                    ratingApi.rate(RatingRequest("TRACK", payload.targetId, payload.value))
                        .toEntity(RatingTargetType.TRACK)
                )
            }
            PendingOpType.ADD_PLAYLIST_TRACK -> {
                val payload = PendingOpJson.decodeFromString<AddPlaylistTrackOpPayload>(op.payloadJson)
                val tracks = playlistDao.getTracksForPlaylistOnce(payload.playlistId)
                playlistApi.setTracks(
                    payload.playlistId,
                    tracks.mapIndexed { i, t -> TrackPositionRequest(t.id, i) },
                )
            }
            PendingOpType.REMOVE_PLAYLIST_TRACK -> {
                val payload = PendingOpJson.decodeFromString<RemovePlaylistTrackOpPayload>(op.payloadJson)
                val tracks = playlistDao.getTracksForPlaylistOnce(payload.playlistId)
                playlistApi.setTracks(
                    payload.playlistId,
                    tracks.mapIndexed { i, t -> TrackPositionRequest(t.id, i) },
                )
            }
            PendingOpType.REORDER_PLAYLIST_TRACKS -> {
                val payload = PendingOpJson.decodeFromString<ReorderPlaylistTracksOpPayload>(op.payloadJson)
                playlistApi.setTracks(
                    payload.playlistId,
                    payload.orderedTrackIds.mapIndexed { i, id -> TrackPositionRequest(id, i) },
                )
            }
            PendingOpType.UPLOAD_TRACK -> {
                val payload = PendingOpJson.decodeFromString<UploadTrackOpPayload>(op.payloadJson)
                val file = File(payload.localFilePath)
                if (!file.exists()) return
                val textPlain = "text/plain".toMediaType()
                val filePart = MultipartBody.Part.createFormData(
                    "file", file.name, file.asRequestBody("audio/*".toMediaType())
                )
                uploadApi.uploadTrack(
                    title = payload.title.toRequestBody(textPlain),
                    artist = payload.artist.toRequestBody(textPlain),
                    album = payload.album.takeIf { it.isNotBlank() }?.toRequestBody(textPlain),
                    genre = payload.genre.toRequestBody(textPlain),
                    file = filePart,
                )
                file.delete()
            }
            PendingOpType.UNKNOWN -> Unit
            else -> Unit
        }
    }

    private fun PlaylistSummaryResponse.toEntity(): PlaylistEntity =
        PlaylistEntity(
            id = id,
            remoteId = id,
            name = name,
            ownerId = LOCAL_OWNER,
            isMine = true,
            isCopy = isCopy,
            sourcePlaylistId = sourcePlaylistId,
            ratingAvg = ratingAvg.toFloat(),
            updatedAt = updatedAt?.let(Instant::parse) ?: Instant.now(),
        )

    private fun RatingResponse.toEntity(targetType: RatingTargetType): RatingEntity =
        RatingEntity(
            id = id,
            targetType = targetType,
            targetId = targetId,
            value = value,
            syncedAt = updatedAt?.let(Instant::parse) ?: Instant.now(),
        )

    private companion object {
        const val PREFS_NAME = "serenade_sync"
        const val KEY_CURSOR = "changes_cursor"
        const val INITIAL_CURSOR = "1970-01-01T00:00:00Z"
        const val LOCAL_OWNER = "me"
    }
}
