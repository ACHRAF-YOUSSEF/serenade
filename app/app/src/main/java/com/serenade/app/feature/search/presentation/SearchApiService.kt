package com.serenade.app.feature.search.presentation

import com.serenade.app.feature.track.data.remote.dto.PageResponse
import com.serenade.app.feature.track.data.remote.dto.TrackResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApiService {
    @GET("api/search")
    suspend fun search(
        @Query("q") q: String = "",
        @Query("genres") genres: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): PageResponse<TrackResponse>
}
