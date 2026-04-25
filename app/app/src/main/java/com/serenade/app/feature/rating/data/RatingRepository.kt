package com.serenade.app.feature.rating.data

import com.serenade.app.feature.rating.data.entity.RatingEntity
import com.serenade.app.feature.rating.data.entity.RatingTargetType
import com.serenade.app.feature.rating.data.remote.RatingApiService
import com.serenade.app.feature.rating.data.remote.dto.RatingRequest
import com.serenade.app.feature.sync.data.PendingOpDao
import com.serenade.app.feature.sync.data.entity.PendingOpEntity
import com.serenade.app.feature.sync.data.entity.PendingOpJson
import com.serenade.app.feature.sync.data.entity.PendingOpType
import com.serenade.app.feature.sync.data.entity.RateOpPayload
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.encodeToString

@Singleton
class RatingRepository @Inject constructor(
    private val api: RatingApiService,
    private val ratingDao: RatingDao,
    private val pendingOpDao: PendingOpDao,
) {
    suspend fun ratePlaylist(playlistId: String, value: Int) {
        rate(RatingTargetType.PLAYLIST, PendingOpType.RATE_PLAYLIST, playlistId, value)
    }

    suspend fun rateTrack(trackId: String, value: Int) {
        rate(RatingTargetType.TRACK, PendingOpType.RATE_TRACK, trackId, value)
    }

    private suspend fun rate(
        targetType: RatingTargetType,
        opType: PendingOpType,
        targetId: String,
        value: Int,
    ) {
        require(value in 1..5) { "Rating must be between 1 and 5" }

        val localRating = RatingEntity(
            id = "local-${targetType.name}-$targetId",
            targetType = targetType,
            targetId = targetId,
            value = value,
            syncedAt = Instant.now(),
        )
        ratingDao.insert(localRating)

        val opId = UUID.randomUUID().toString()
        pendingOpDao.insert(
            PendingOpEntity(
                id = opId,
                type = opType,
                payloadJson = PendingOpJson.encodeToString(RateOpPayload(targetId, value)),
                createdAt = Instant.now(),
            )
        )

        runCatching {
            api.rate(RatingRequest(targetType.name, targetId, value))
        }.onSuccess { remote ->
            ratingDao.insert(
                RatingEntity(
                    id = remote.id,
                    targetType = targetType,
                    targetId = remote.targetId,
                    value = remote.value,
                    syncedAt = remote.updatedAt?.let(Instant::parse) ?: Instant.now(),
                )
            )
            ratingDao.deleteById(localRating.id)
            pendingOpDao.deleteById(opId)
        }
    }
}
