package com.serenade.app.feature.track.data.remote

import com.serenade.app.feature.track.data.remote.dto.PageResponse
import com.serenade.app.feature.track.data.remote.dto.TrackResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TrackApiService {
    @GET("api/tracks")
    suspend fun getTracks(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
        @Query("sort") sort: String = "updatedAt,desc",
    ): PageResponse<TrackResponse>
}
