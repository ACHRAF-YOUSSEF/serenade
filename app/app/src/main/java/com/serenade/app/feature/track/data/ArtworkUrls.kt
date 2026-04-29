package com.serenade.app.feature.track.data

import com.serenade.app.BuildConfig

fun stableArtworkUrl(trackId: String, artworkUrl: String?): String? {
    if (artworkUrl.isNullOrBlank()) return null
    return "${BuildConfig.API_BASE_URL.trimEnd('/')}/artwork/$trackId"
}
