package com.serenade.app.feature.player.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaStyleNotificationHelper
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.serenade.app.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SerenadePlayerService : MediaSessionService() {

    @Inject
    lateinit var player: ExoPlayer

    private var mediaSession: MediaSession? = null
    private val notificationRefreshListener = object : Player.Listener {
        @OptIn(UnstableApi::class)
        override fun onEvents(player: Player, events: Player.Events) {
            if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION) ||
                events.contains(Player.EVENT_MEDIA_METADATA_CHANGED) ||
                events.contains(Player.EVENT_IS_PLAYING_CHANGED) ||
                events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED) ||
                events.contains(Player.EVENT_TIMELINE_CHANGED)
            ) {
                ensureForeground()
                triggerNotificationUpdate()
            }
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        ensureForeground()
        val mediaNotificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setNotificationId(NOTIFICATION_ID)
            .build()
        mediaNotificationProvider.setSmallIcon(R.drawable.ic_notification_music)
        setMediaNotificationProvider(
            mediaNotificationProvider
        )
        mediaSession = MediaSession.Builder(this, player).build()
        player.addListener(notificationRefreshListener)
    }

    @OptIn(UnstableApi::class)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ensureForeground()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        player.removeListener(notificationRefreshListener)
        mediaSession?.run {
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    @OptIn(UnstableApi::class)
    private fun ensureForeground() {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Player", NotificationManager.IMPORTANCE_LOW)
            )
        }

        val metadata = player.mediaMetadata
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_music)
            .setContentTitle(metadata.title ?: getString(R.string.app_name))
            .setContentText(metadata.artist)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setOngoing(player.isPlaying)
            .setSilent(true)
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        mediaSession?.let { session ->
            notificationBuilder.setStyle(MediaStyleNotificationHelper.MediaStyle(session))
        }

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notificationBuilder.build(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK else 0
        )
    }

    companion object {
        private const val CHANNEL_ID = "player_service"
        private const val NOTIFICATION_ID = 1
    }
}
