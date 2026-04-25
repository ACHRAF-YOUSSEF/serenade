package com.serenade.app.feature.upload.data

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.provider.OpenableColumns
import com.serenade.app.core.database.Genre
import com.serenade.app.feature.sync.data.PendingOpDao
import com.serenade.app.feature.sync.data.entity.PendingOpEntity
import com.serenade.app.feature.sync.data.entity.PendingOpJson
import com.serenade.app.feature.sync.data.entity.PendingOpType
import com.serenade.app.feature.sync.data.entity.UploadTrackOpPayload
import com.serenade.app.feature.upload.data.remote.UploadApiService
import com.serenade.app.feature.upload.data.remote.UploadResponse
import com.serenade.app.feature.upload.data.remote.UploadStatusResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import java.io.File
import java.io.IOException
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class UploadFileInfo(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long?,
    val contentType: String?,
)

@Singleton
class UploadRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val api: UploadApiService,
    private val pendingOpDao: PendingOpDao,
) {
    private val textPlain = "text/plain".toMediaType()

    fun inspect(uri: Uri): UploadFileInfo {
        val resolver = context.contentResolver
        val cursorInfo = resolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                CursorFileInfo(
                    name = cursor.stringValue(OpenableColumns.DISPLAY_NAME),
                    sizeBytes = cursor.longValue(OpenableColumns.SIZE),
                )
            } else {
                null
            }
        }
        val name = cursorInfo?.name
            ?: uri.lastPathSegment?.substringAfterLast('/')
            ?: "audio"
        return UploadFileInfo(
            uri = uri,
            name = name,
            sizeBytes = cursorInfo?.sizeBytes?.takeIf { it >= 0L },
            contentType = resolver.getType(uri),
        )
    }

    suspend fun upload(
        uri: Uri,
        title: String,
        artist: String,
        album: String,
        genre: Genre,
        onProgress: (bytesWritten: Long, contentLength: Long) -> Unit,
    ): UploadResponse {
        val info = inspect(uri)
        val resolvedType = when {
            info.contentType == "video/mp4" && info.name.endsWith(".m4a", ignoreCase = true) -> "audio/mp4"
            info.contentType != null -> info.contentType
            else -> "application/octet-stream"
        }
        val mediaType = resolvedType.toMediaType()
        val body = ContentUriRequestBody(
            resolver = context.contentResolver,
            uri = uri,
            mediaType = mediaType,
            contentLength = info.sizeBytes ?: -1L,
            onProgress = onProgress,
        )
        val part = MultipartBody.Part.createFormData("file", info.name, body)
        return api.uploadTrack(
            title = title.trim().toRequestBody(textPlain),
            artist = artist.trim().toRequestBody(textPlain),
            album = album.trim().takeIf { it.isNotBlank() }?.toRequestBody(textPlain),
            genre = genre.name.toRequestBody(textPlain),
            file = part,
        )
    }

    suspend fun status(trackId: String): UploadStatusResponse = api.getUploadStatus(trackId)

    fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun queueUpload(
        uri: Uri,
        title: String,
        artist: String,
        album: String,
        genre: Genre,
    ) {
        val pendingDir = File(context.filesDir, "pending_uploads")
        pendingDir.mkdirs()
        val destFile = File(pendingDir, "${UUID.randomUUID()}.audio")
        context.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { out -> input.copyTo(out) }
        } ?: throw IOException("Cannot open selected file")

        pendingOpDao.insert(
            PendingOpEntity(
                id = UUID.randomUUID().toString(),
                type = PendingOpType.UPLOAD_TRACK,
                payloadJson = PendingOpJson.encodeToString(
                    UploadTrackOpPayload(
                        localFilePath = destFile.absolutePath,
                        title = title,
                        artist = artist,
                        album = album,
                        genre = genre.name,
                    )
                ),
                createdAt = Instant.now(),
            )
        )
    }

    private data class CursorFileInfo(
        val name: String?,
        val sizeBytes: Long?,
    )
}

private class ContentUriRequestBody(
    private val resolver: ContentResolver,
    private val uri: Uri,
    private val mediaType: MediaType,
    private val contentLength: Long,
    private val onProgress: (bytesWritten: Long, contentLength: Long) -> Unit,
) : RequestBody() {
    override fun contentType(): MediaType = mediaType

    override fun contentLength(): Long = contentLength

    override fun writeTo(sink: BufferedSink) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var uploaded = 0L
        resolver.openInputStream(uri)?.use { input ->
            while (true) {
                val read = input.read(buffer)
                if (read == -1) break
                sink.write(buffer, 0, read)
                uploaded += read
                onProgress(uploaded, contentLength)
            }
        } ?: throw IOException("Unable to open selected file")
    }
}

private fun Cursor.stringValue(columnName: String): String? {
    val index = getColumnIndex(columnName)
    return if (index >= 0 && !isNull(index)) getString(index) else null
}

private fun Cursor.longValue(columnName: String): Long? {
    val index = getColumnIndex(columnName)
    return if (index >= 0 && !isNull(index)) getLong(index) else null
}
