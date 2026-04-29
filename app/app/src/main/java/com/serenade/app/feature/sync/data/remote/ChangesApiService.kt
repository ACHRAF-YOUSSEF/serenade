package com.serenade.app.feature.sync.data.remote

import com.serenade.app.feature.sync.data.remote.dto.ChangesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ChangesApiService {
    @GET("api/changes")
    suspend fun getChanges(
        @Query("since") since: String,
        @Query("limit") limit: Int = 100,
    ): ChangesResponse
}
