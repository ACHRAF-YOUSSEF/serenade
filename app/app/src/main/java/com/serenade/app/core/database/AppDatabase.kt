package com.serenade.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.serenade.app.feature.auth.data.UserDao
import com.serenade.app.feature.auth.data.entity.UserEntity
import com.serenade.app.feature.download.data.DownloadDao
import com.serenade.app.feature.download.data.entity.DownloadEntity
import com.serenade.app.feature.playlist.data.PlaylistDao
import com.serenade.app.feature.playlist.data.entity.PlaylistEntity
import com.serenade.app.feature.playlist.data.entity.PlaylistTrackCrossRef
import com.serenade.app.feature.providers.data.ProviderDao
import com.serenade.app.feature.providers.data.entity.ProviderEntity
import com.serenade.app.feature.rating.data.RatingDao
import com.serenade.app.feature.rating.data.entity.RatingEntity
import com.serenade.app.feature.subtitle.data.SubtitleDao
import com.serenade.app.feature.subtitle.data.entity.SubtitleLineEntity
import com.serenade.app.feature.sync.data.PendingOpDao
import com.serenade.app.feature.sync.data.entity.PendingOpEntity
import com.serenade.app.feature.track.data.TrackDao
import com.serenade.app.feature.track.data.entity.TrackEntity

@Database(
    entities = [
        UserEntity::class,
        TrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class,
        SubtitleLineEntity::class,
        DownloadEntity::class,
        PendingOpEntity::class,
        ProviderEntity::class,
        RatingEntity::class,
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(AppConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun subtitleDao(): SubtitleDao
    abstract fun downloadDao(): DownloadDao
    abstract fun pendingOpDao(): PendingOpDao
    abstract fun providerDao(): ProviderDao
    abstract fun ratingDao(): RatingDao

    companion object {
        const val DATABASE_NAME = "serenade.db"
    }
}
