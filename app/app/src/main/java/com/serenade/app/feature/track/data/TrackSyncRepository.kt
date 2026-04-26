package com.serenade.app.feature.track.data

import com.serenade.app.core.database.Genre
import com.serenade.app.feature.track.data.entity.TrackEntity
import com.serenade.app.feature.track.data.remote.TrackApiService
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackSyncRepository @Inject constructor(
    private val api: TrackApiService,
    private val dao: TrackDao,
) {
    fun tracks(): Flow<List<TrackEntity>> = dao.getAll()

    suspend fun sync() {
        val page = api.getTracks()
        val entities = page.content.map { r ->
            TrackEntity(
                id = r.id,
                remoteId = r.id,
                title = r.title,
                artist = r.artist,
                album = r.album ?: "",
                genre = runCatching { Genre.valueOf(r.genre) }.getOrDefault(Genre.OTHER),
                durationMs = r.durationMs ?: 0L,
                artworkUrl = r.artworkUrl,
                localPath = null,
                isDownloaded = false,
                streamUrl = r.streamUrl,
                streamUrlExpiresAt = null,
                providerId = "serenade",
                updatedAt = Instant.now(),
            )
        }
        dao.upsertAllFromRemote(entities)
    }
}
