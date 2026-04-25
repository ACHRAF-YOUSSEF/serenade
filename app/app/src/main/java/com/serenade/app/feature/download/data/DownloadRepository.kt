package com.serenade.app.feature.download.data

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.serenade.app.feature.download.data.entity.DownloadEntity
import com.serenade.app.feature.download.data.entity.DownloadState
import com.serenade.app.feature.download.worker.DownloadWorker
import com.serenade.app.feature.track.data.TrackDao
import com.serenade.app.feature.track.data.entity.TrackEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val downloadDao: DownloadDao,
    private val trackDao: TrackDao,
) {
    fun downloads(): Flow<List<DownloadEntity>> = downloadDao.getAll()

    fun downloadedTracks(): Flow<List<TrackEntity>> = trackDao.getDownloaded()

    suspend fun queueDownload(track: TrackEntity) {
        if (track.streamUrl.isNullOrBlank()) return

        downloadDao.insert(
            DownloadEntity(
                id = track.id,
                trackId = track.id,
                state = DownloadState.QUEUED,
                progress = 0,
                filePath = track.localPath,
            )
        )

        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(
                workDataOf(
                    DownloadWorker.KEY_TRACK_ID to track.id,
                    DownloadWorker.KEY_STREAM_URL to track.streamUrl,
                    DownloadWorker.KEY_TITLE to track.title,
                )
            )
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            DownloadWorker.workName(track.id),
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    suspend fun deleteDownload(trackId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(DownloadWorker.workName(trackId))
        val existing = downloadDao.getByTrackIdOnce(trackId)
        existing?.filePath?.let { File(it).delete() }
        downloadDao.deleteByTrackId(trackId)
        trackDao.updateDownloadState(trackId, localPath = null, isDownloaded = false)
    }
}
