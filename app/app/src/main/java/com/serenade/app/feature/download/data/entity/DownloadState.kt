package com.serenade.app.feature.download.data.entity

enum class DownloadState {
    QUEUED,
    DOWNLOADING,
    PAUSED,
    DONE,
    FAILED
}