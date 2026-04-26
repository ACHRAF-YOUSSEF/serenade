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
import com.serenade.app.core.di.PublicOkHttpClient
import com.serenade.app.feature.download.data.DownloadDao
import com.serenade.app.feature.download.data.entity.DownloadState
import com.serenade.app.feature.track.data.TrackDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.Locale

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @param:PublicOkHttpClient
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
            cleanupPartial(trackId)
            val localPath = if (streamUrl.isHlsManifestUrl()) {
                downloadHlsPackage(trackId, streamUrl)
            } else {
                downloadSingleFile(trackId, streamUrl)
            }

            downloadDao.updateForTrack(trackId, DownloadState.DONE, 100, localPath)
            trackDao.updateDownloadState(
                trackId,
                localPath = localPath,
                isDownloaded = true
            )
            notifyComplete(title)
            Result.success()
        }.getOrElse {
            cleanupPartial(trackId)
            downloadDao.updateForTrack(trackId, DownloadState.FAILED, 0, null)
            Result.failure()
        }
    }

    private suspend fun downloadSingleFile(trackId: String, streamUrl: String): String {
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
                            val progress =
                                ((writtenBytes * 100) / totalBytes).toInt().coerceIn(0, 99)
                            downloadDao.updateForTrack(
                                trackId,
                                DownloadState.DOWNLOADING,
                                progress,
                                target.absolutePath
                            )
                        }
                    }
                }
            }
        }
        return target.absolutePath
    }

    private suspend fun downloadHlsPackage(trackId: String, manifestUrl: String): String {
        val targetDir = targetDirectory(trackId)
        targetDir.mkdirs()
        val manifestFile = File(targetDir, "index.m3u8")

        val manifestRequest = Request.Builder().url(manifestUrl).build()
        val manifestResponse = client.newCall(manifestRequest).execute()
        val manifestText = manifestResponse.use { response ->
            if (!response.isSuccessful) error("Manifest download failed: HTTP ${response.code}")
            response.body.string()
        }
        downloadDao.updateForTrack(trackId, DownloadState.DOWNLOADING, 10, manifestFile.absolutePath)

        val lines = manifestText.lineSequence().toList()
        val segmentLines = lines.withIndex()
            .filter { (_, line) -> line.isSegmentReference() }
        val rewritten = lines.toMutableList()

        segmentLines.forEachIndexed { segmentIndex, indexedLine ->
            val segmentUrl = manifestRequest.url.resolve(indexedLine.value.trim())
                ?: error("Invalid HLS segment URL")
            val segmentFile = File(
                targetDir,
                "${segmentIndex.toString().padStart(5, '0')}${segmentUrl.segmentExtension()}"
            )
            downloadUrlToFile(segmentUrl.toString(), segmentFile)
            rewritten[indexedLine.index] = segmentFile.name
            val progress = (10 + ((segmentIndex + 1) * 89 / segmentLines.size.coerceAtLeast(1)))
                .coerceIn(10, 99)
            downloadDao.updateForTrack(
                trackId,
                DownloadState.DOWNLOADING,
                progress,
                manifestFile.absolutePath
            )
        }

        manifestFile.writeText(rewritten.joinToString(separator = "\n", postfix = "\n"))
        return manifestFile.absolutePath
    }

    private fun downloadUrlToFile(url: String, target: File) {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Segment download failed: HTTP ${response.code}")
            response.body.byteStream().use { input ->
                target.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    private fun String.isHlsManifestUrl(): Boolean {
        return substringBefore('?')
            .lowercase(Locale.US)
            .endsWith(".m3u8")
    }

    private fun String.isSegmentReference(): Boolean {
        val trimmed = trim()
        return trimmed.isNotEmpty() && !trimmed.startsWith("#")
    }

    private fun okhttp3.HttpUrl.segmentExtension(): String {
        val fileName = pathSegments.lastOrNull().orEmpty()
        val extension = fileName.substringAfterLast('.', missingDelimiterValue = "")
        return if (extension.isBlank()) ".segment" else ".$extension"
    }

    private fun cleanupPartial(trackId: String) {
        targetFile(trackId).delete()
        targetDirectory(trackId).deleteRecursively()
    }

    private fun targetDirectory(trackId: String): File {
        return File(applicationContext.filesDir, "downloads/$trackId")
    }

    private fun targetFile(trackId: String): File {
        return File(applicationContext.filesDir, "downloads/$trackId.audio")
    }

    private fun notifyComplete(title: String) {
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_DEFAULT,
            )
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
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
