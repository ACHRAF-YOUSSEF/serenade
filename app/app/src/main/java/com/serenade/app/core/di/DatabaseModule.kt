package com.serenade.app.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.serenade.app.BuildConfig
import com.serenade.app.core.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val builder = Room.databaseBuilder(
            context = context,
            klass = AppDatabase::class.java,
            name = AppDatabase.DATABASE_NAME
        )

        builder.addMigrations(MIGRATION_1_2)

        if (BuildConfig.DEBUG) {
            builder.fallbackToDestructiveMigration(dropAllTables = true)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase) = db.userDao()

    @Provides
    @Singleton
    fun provideTrackDao(db: AppDatabase) = db.trackDao()

    @Provides
    @Singleton
    fun providePlaylistDao(db: AppDatabase) = db.playlistDao()

    @Provides
    @Singleton
    fun provideSubtitleDao(db: AppDatabase) = db.subtitleDao()

    @Provides
    @Singleton
    fun provideDownloadDao(db: AppDatabase) = db.downloadDao()

    @Provides
    @Singleton
    fun providePendingOpDao(db: AppDatabase) = db.pendingOpDao()

    @Provides
    @Singleton
    fun provideProviderDao(db: AppDatabase) = db.providerDao()

    @Provides
    @Singleton
    fun provideRatingDao(db: AppDatabase) = db.ratingDao()

    @Provides
    @Singleton
    fun providePlaybackDao(db: AppDatabase) = db.playbackDao()

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS `playback_queue`")
            db.execSQL("DROP TABLE IF EXISTS `playback_history`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `playback_queue` (
                    `queuePosition` INTEGER NOT NULL,
                    `trackId` TEXT NOT NULL,
                    `streamUrl` TEXT NOT NULL,
                    `title` TEXT,
                    `artist` TEXT,
                    `album` TEXT,
                    `durationMs` INTEGER NOT NULL,
                    `artworkUrl` TEXT,
                    `isCurrent` INTEGER NOT NULL,
                    `positionMs` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`queuePosition`)
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_playback_queue_trackId` ON `playback_queue` (`trackId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_playback_queue_isCurrent` ON `playback_queue` (`isCurrent`)")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `playback_history` (
                    `trackId` TEXT NOT NULL,
                    `title` TEXT,
                    `artist` TEXT,
                    `album` TEXT,
                    `durationMs` INTEGER NOT NULL,
                    `artworkUrl` TEXT,
                    `lastPositionMs` INTEGER NOT NULL,
                    `lastPlayedAt` INTEGER NOT NULL,
                    `playCount` INTEGER NOT NULL,
                    PRIMARY KEY(`trackId`)
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_playback_history_lastPlayedAt` ON `playback_history` (`lastPlayedAt`)")
        }
    }
}
