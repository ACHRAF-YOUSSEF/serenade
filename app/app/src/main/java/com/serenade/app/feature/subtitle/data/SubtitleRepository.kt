package com.serenade.app.feature.subtitle.data

import com.serenade.app.feature.subtitle.data.entity.SubtitleLineEntity
import com.serenade.app.feature.subtitle.data.remote.SubtitleApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubtitleRepository @Inject constructor(
    private val api: SubtitleApiService,
) {
    suspend fun getSubtitles(trackId: String): List<SubtitleLineEntity> {
        return try {
            api.getSubtitles(trackId).map { r ->
                SubtitleLineEntity(r.id, trackId, r.startMs, r.endMs, r.text)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
