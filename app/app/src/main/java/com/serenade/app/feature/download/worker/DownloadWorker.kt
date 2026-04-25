package com.serenade.app.feature.download.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.serenade.app.feature.download.data.DownloadDao
import com.serenade.app.feature.download.data.entity.DownloadState
import com.serenade.app.feature.track.data.TrackDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val client: OkHttpClient,
    private val downloadDao: DownloadDao,
    private val trackDao: TrackDao,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val trackId = inputData.getString(KEY_TRACK_ID) ?: return Result.failure()
        val streamUrl = inputData.getString(KEY_STREAM_URL) ?: return Result.failure()
        val title = inputData.getString(KEY_TITLE) ?: "Track"

        return runCatching {
            downloadDao.updateForTrack(trackId, DownloadState.DOWNLOADING, 0, null)
            val target = targetFile(trackId)
            target.parentFile?.mkdirs()

            val request = Request.Builder().url(streamUrl).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("Download failed: HTTP ${response.code}")
                val body = response.body
                val totalBytes = body.contentLength()
                var writtenBytes = 0L

                body.byteStream().use { input ->
                    target.outputStream().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            output.write(buffer, 0, read)
                            writtenBytes += read
                            if (totalBytes > 0) {
                                val progress = ((writtenBytes * 100) / totalBytes).toInt().coerceIn(0, 99)
                                downloadDao.updateForTrack(trackId, DownloadState.DOWNLOADING, progress, target.absolutePath)
                            }
                        }
                    }
                }
            }

            downloadDao.updateForTrack(trackId, DownloadState.DONE, 100, target.absolutePath)
            trackDao.updateDownloadState(trackId, localPath = target.absolutePath, isDownloaded = true)
            notifyComplete(title)
            Result.success()
        }.getOrElse {
            downloadDao.updateForTrack(trackId, DownloadState.FAILED, 0, null)
            targetFile(trackId).delete()
            Result.failure()
        }
    }

    private fun targetFile(trackId: String): File {
        return File(applicationContext.filesDir, "downloads/$trackId.audio")
    }

    private fun notifyComplete(title: String) {
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Downloads",
                    NotificationManager.IMPORTANCE_DEFAULT,
                )
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Download complete")
            .setContentText(title)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(title.hashCode(), notification)
    }

    companion object {
        const val KEY_TRACK_ID = "trackId"
        const val KEY_STREAM_URL = "streamUrl"
        const val KEY_TITLE = "title"
        private const val CHANNEL_ID = "downloads"

        fun workName(trackId: String) = "download:$trackId"
    }
}
