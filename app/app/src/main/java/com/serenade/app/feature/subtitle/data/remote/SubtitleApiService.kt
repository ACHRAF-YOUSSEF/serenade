package com.serenade.app.feature.subtitle.data.remote

import com.serenade.app.feature.subtitle.data.remote.dto.SubtitleLineResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface SubtitleApiService {
    @GET("api/tracks/{id}/subtitles")
    suspend fun getSubtitles(@Path("id") trackId: String): List<SubtitleLineResponse>
}
