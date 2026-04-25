package com.serenade.app.core.database

import androidx.room.TypeConverter
import com.serenade.app.feature.download.data.entity.DownloadState
import com.serenade.app.feature.rating.data.entity.RatingTargetType
import com.serenade.app.feature.sync.data.entity.PendingOpType
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

class AppConverters {
    @TypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun toInstant(millis: Long?): Instant? = millis?.let {
        Instant.ofEpochMilli(it)
    }

    @TypeConverter
    fun fromGenre(genre: Genre?): String? = genre?.name

    @TypeConverter
    fun toGenre(name: String?): Genre = enumByName(name) ?: Genre.OTHER

    @TypeConverter
    fun fromDownloadState(state: DownloadState?): String? = state?.name

    @TypeConverter
    fun toDownloadState(name: String?): DownloadState = enumByName(name) ?: DownloadState.FAILED

    @TypeConverter
    fun fromRatingTargetType(type: RatingTargetType?): String? = type?.name

    @TypeConverter
    fun toRatingTargetType(name: String?): RatingTargetType = enumByName(name) ?: RatingTargetType.UNKNOWN

    @TypeConverter
    fun fromPendingOpType(type: PendingOpType?): String? = type?.name

    @TypeConverter
    fun toPendingOpType(name: String?): PendingOpType = enumByName(name) ?: PendingOpType.UNKNOWN

    @TypeConverter
    fun fromStringList(value: List<String>?): String? = value?.let {
        Json.encodeToString(it)
    }

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()

        return try {
            Json.decodeFromString<List<String>>(json)
        } catch (_: SerializationException) {
            emptyList()
        } catch (_: IllegalArgumentException) {
            emptyList()
        }
    }

    private inline fun <reified T : Enum<T>> enumByName(name: String?): T? {
        if (name == null) return null
        return enumValues<T>().firstOrNull { it.name == name }
    }
}
