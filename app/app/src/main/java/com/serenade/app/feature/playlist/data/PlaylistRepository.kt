package com.serenade.app.feature.playlist.data

import com.serenade.app.core.database.Genre
import com.serenade.app.feature.playlist.data.entity.PlaylistEntity
import com.serenade.app.feature.playlist.data.entity.PlaylistTrackCrossRef
import com.serenade.app.feature.playlist.data.remote.PlaylistApiService
import com.serenade.app.feature.playlist.data.remote.dto.CreatePlaylistRequest
import com.serenade.app.feature.playlist.data.remote.dto.PlaylistDetailResponse
import com.serenade.app.feature.playlist.data.remote.dto.PlaylistSummaryResponse
import com.serenade.app.feature.sync.data.PendingOpDao
import com.serenade.app.feature.sync.data.entity.CopyPlaylistOpPayload
import com.serenade.app.feature.sync.data.entity.CreatePlaylistOpPayload
import com.serenade.app.feature.sync.data.entity.PendingOpEntity
import com.serenade.app.feature.sync.data.entity.PendingOpJson
import com.serenade.app.feature.sync.data.entity.PendingOpType
import com.serenade.app.feature.track.data.TrackDao
import com.serenade.app.feature.track.data.entity.TrackEntity
import com.serenade.app.feature.track.data.remote.dto.TrackResponse
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString

@Singleton
class PlaylistRepository @Inject constructor(
    private val api: PlaylistApiService,
    private val playlistDao: PlaylistDao,
    private val trackDao: TrackDao,
    private val pendingOpDao: PendingOpDao,
) {
    fun playlists(): Flow<List<PlaylistSummaryResponse>> =
        playlistDao.getAllWithTrackCounts().map { rows ->
            rows.map { it.toSummary() }
        }

    suspend fun refresh() {
        api.getMyPlaylists().content.forEach { playlistDao.insert(it.toEntity()) }
    }

    suspend fun getDetail(playlistId: String): PlaylistDetailResponse {
        return runCatching { api.getPlaylist(playlistId) }
            .onSuccess { detail -> cacheDetail(detail) }
            .getOrElse { localDetail(playlistId) }
    }

    suspend fun createPlaylist(name: String): PlaylistSummaryResponse {
        require(name.isNotBlank()) { "Playlist name is required" }

        val localId = UUID.randomUUID().toString()
        val opId = UUID.randomUUID().toString()
        val now = Instant.now()
        val local = PlaylistEntity(
            id = localId,
            remoteId = null,
            name = name,
            ownerId = LOCAL_OWNER,
            isMine = true,
            isCopy = false,
            sourcePlaylistId = null,
            ratingAvg = 0f,
            updatedAt = now,
        )
        playlistDao.insert(local)
        pendingOpDao.insert(
            PendingOpEntity(
                id = opId,
                type = PendingOpType.CREATE_PLAYLIST,
                payloadJson = PendingOpJson.encodeToString(CreatePlaylistOpPayload(localId, name)),
                createdAt = now,
            )
        )

        return runCatching { api.createPlaylist(CreatePlaylistRequest(name)) }
            .onSuccess { remote ->
                playlistDao.insert(remote.toEntity())
                playlistDao.deleteById(localId)
                pendingOpDao.deleteById(opId)
            }
            .getOrElse { local.toSummary(trackCount = 0) }
    }

    suspend fun copyPlaylist(sourcePlaylistId: String): PlaylistSummaryResponse? {
        val opId = UUID.randomUUID().toString()
        pendingOpDao.insert(
            PendingOpEntity(
                id = opId,
                type = PendingOpType.COPY_PLAYLIST,
                payloadJson = PendingOpJson.encodeToString(CopyPlaylistOpPayload(sourcePlaylistId)),
                createdAt = Instant.now(),
            )
        )

        return runCatching { api.copyPlaylist(sourcePlaylistId) }
            .onSuccess { remote ->
                playlistDao.insert(remote.toEntity())
                pendingOpDao.deleteById(opId)
            }
            .getOrNull()
    }

    private suspend fun cacheDetail(detail: PlaylistDetailResponse) {
        playlistDao.insert(
            PlaylistEntity(
                id = detail.id,
                remoteId = detail.id,
                name = detail.name,
                ownerId = LOCAL_OWNER,
                isMine = true,
                isCopy = detail.isCopy,
                sourcePlaylistId = detail.sourcePlaylistId,
                ratingAvg = detail.ratingAvg.toFloat(),
                updatedAt = Instant.now(),
            )
        )
        playlistDao.clearTracksForPlaylist(detail.id)
        detail.tracks.forEachIndexed { index, track ->
            trackDao.insert(track.toEntity())
            playlistDao.insertCrossRef(
                PlaylistTrackCrossRef(
                    playlistId = detail.id,
                    trackId = track.id,
                    position = index,
                )
            )
        }
    }

    private suspend fun localDetail(playlistId: String): PlaylistDetailResponse {
        val playlist = playlistDao.getByIdOnce(playlistId)
            ?: throw IllegalStateException("Playlist not found")
        val tracks = playlistDao.getTracksForPlaylistOnce(playlistId).map { it.toResponse() }
        return PlaylistDetailResponse(
            id = playlist.id,
            name = playlist.name,
            isCopy = playlist.isCopy,
            sourcePlaylistId = playlist.sourcePlaylistId,
            version = 0,
            ratingAvg = playlist.ratingAvg.toDouble(),
            tracks = tracks,
        )
    }

    private fun PlaylistWithTrackCount.toSummary(): PlaylistSummaryResponse =
        playlist.toSummary(trackCount = trackCount)

    private fun PlaylistEntity.toSummary(trackCount: Int): PlaylistSummaryResponse =
        PlaylistSummaryResponse(
            id = id,
            name = name,
            isCopy = isCopy,
            sourcePlaylistId = sourcePlaylistId,
            version = 0,
            trackCount = trackCount,
            ratingAvg = ratingAvg.toDouble(),
            updatedAt = updatedAt.toString(),
        )

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

    private fun TrackResponse.toEntity(): TrackEntity =
        TrackEntity(
            id = id,
            remoteId = id,
            title = title,
            artist = artist,
            album = album ?: "",
            genre = runCatching { Genre.valueOf(genre) }.getOrDefault(Genre.OTHER),
            durationMs = durationMs ?: 0L,
            artworkUrl = artworkUrl,
            localPath = null,
            streamUrl = streamUrl,
            streamUrlExpiresAt = null,
            isDownloaded = false,
            providerId = "serenade",
            updatedAt = updatedAt?.let(Instant::parse) ?: Instant.now(),
        )

    private fun TrackEntity.toResponse(): TrackResponse =
        TrackResponse(
            id = id,
            title = title,
            artist = artist,
            album = album,
            genre = genre.name,
            durationMs = durationMs,
            artworkUrl = artworkUrl,
            streamUrl = streamUrl,
            updatedAt = updatedAt.toString(),
        )

    private companion object {
        const val LOCAL_OWNER = "me"
    }
}
