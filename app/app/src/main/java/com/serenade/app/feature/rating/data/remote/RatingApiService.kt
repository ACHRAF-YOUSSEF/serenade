package com.serenade.app.feature.rating.data.remote

import com.serenade.app.feature.rating.data.remote.dto.RatingRequest
import com.serenade.app.feature.rating.data.remote.dto.RatingResponse
import retrofit2.http.Body
import retrofit2.http.POST

fun interface RatingApiService {
    @POST("api/ratings")
    suspend fun rate(@Body req: RatingRequest): RatingResponse
}
