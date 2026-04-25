package com.serenade.app.core.navigation

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.serenade.app.core.database.Genre
import com.serenade.app.feature.auth.data.AuthRepository
import com.serenade.app.feature.auth.presentation.LoginScreen
import com.serenade.app.feature.auth.presentation.RegisterScreen
import com.serenade.app.feature.download.presentation.DownloadScreen
import com.serenade.app.feature.player.PlayerController
import com.serenade.app.feature.player.presentation.MiniPlayerBar
import com.serenade.app.feature.player.presentation.PlayerScreen
import com.serenade.app.feature.playlist.presentation.LibraryScreen
import com.serenade.app.feature.playlist.presentation.PlaylistDetailScreen
import com.serenade.app.feature.search.presentation.SearchScreen
import com.serenade.app.feature.track.data.entity.TrackEntity
import com.serenade.app.feature.track.data.remote.dto.TrackResponse
import com.serenade.app.feature.track.presentation.TrackListScreen
import com.serenade.app.feature.upload.presentation.UploadScreen
import java.io.File
import java.time.Instant

private fun TrackResponse.toEntity() = TrackEntity(
    id = id,
    remoteId = id,
    title = title,
    artist = artist,
    album = album ?: "",
    genre = runCatching { Genre.valueOf(genre) }.getOrDefault(Genre.OTHER),
    durationMs = durationMs ?: 0L,
    artworkUrl = artworkUrl,
    localPath = null,
    streamUrl = streamUrl,
    streamUrlExpiresAt = null,
    isDownloaded = false,
    providerId = "serenade",
    updatedAt = Instant.now(),
)

private fun TrackEntity.playbackUri(): String? {
    return localPath?.let { Uri.fromFile(File(it)).toString() } ?: streamUrl
}

private const val ROUTE_LOGIN = "login"
private const val ROUTE_REGISTER = "register"
private const val ROUTE_HOME = "home"
private const val ROUTE_PLAYER = "player"
private const val ROUTE_SEARCH = "search"
private const val ROUTE_LIBRARY = "library"
private const val ROUTE_DOWNLOADS = "downloads"
private const val ROUTE_UPLOAD = "upload"
private const val ROUTE_PLAYLIST_DETAIL = "playlist"
private const val ARG_PLAYLIST_ID = "playlistId"

@Composable
fun AppNavigation(
    authRepository: AuthRepository,
    playerController: PlayerController,
) {
    val navController = rememberNavController()
    val startDestination = if (authRepository.isLoggedIn()) ROUTE_HOME else ROUTE_LOGIN
    val playbackState by playerController.state.collectAsState()

    var nowPlayingTrack by remember { mutableStateOf<TrackEntity?>(null) }

    Scaffold(
        bottomBar = {
            MiniPlayerBar(
                state = playbackState,
                trackTitle = nowPlayingTrack?.title,
                trackArtist = nowPlayingTrack?.artist,
                onTogglePlayPause = playerController::togglePlayPause,
                onBarClick = {
                    if (playbackState.currentTrackId != null) {
                        navController.navigate(ROUTE_PLAYER)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(ROUTE_LOGIN) {
                LoginScreen(
                    onSuccess = {
                        navController.navigate(ROUTE_HOME) {
                            popUpTo(ROUTE_LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(ROUTE_REGISTER) },
                    viewModel = hiltViewModel(),
                )
            }
            composable(ROUTE_REGISTER) {
                RegisterScreen(
                    onSuccess = {
                        navController.navigate(ROUTE_HOME) {
                            popUpTo(ROUTE_LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() },
                    viewModel = hiltViewModel(),
                )
            }
            composable(ROUTE_HOME) {
                TrackListScreen(
                    onTrackClick = { track ->
                        nowPlayingTrack = track
                        track.playbackUri()?.let { url ->
                            playerController.play(track.id, url)
                        }
                    },
                    onSearchClick = { navController.navigate(ROUTE_SEARCH) },
                    onLibraryClick = { navController.navigate(ROUTE_LIBRARY) },
                    onDownloadsClick = { navController.navigate(ROUTE_DOWNLOADS) },
                    onUploadClick = { navController.navigate(ROUTE_UPLOAD) },
                    modifier = Modifier.fillMaxSize(),
                    viewModel = hiltViewModel(),
                )
            }
            composable(ROUTE_SEARCH) {
                SearchScreen(
                    onTrackClick = { r ->
                        val entity = r.toEntity()
                        nowPlayingTrack = entity
                        entity.streamUrl?.let { url -> playerController.play(entity.id, url) }
                        navController.navigate(ROUTE_PLAYER)
                    },
                    onBack = { navController.popBackStack() },
                    viewModel = hiltViewModel(),
                )
            }
            composable(ROUTE_LIBRARY) {
                LibraryScreen(
                    onPlaylistClick = { playlistId ->
                        navController.navigate("$ROUTE_PLAYLIST_DETAIL/$playlistId")
                    },
                    onBack = { navController.popBackStack() },
                    viewModel = hiltViewModel(),
                )
            }
            composable(ROUTE_DOWNLOADS) {
                DownloadScreen(
                    onTrackClick = { track ->
                        nowPlayingTrack = track
                        track.playbackUri()?.let { url -> playerController.play(track.id, url) }
                        navController.navigate(ROUTE_PLAYER)
                    },
                    onBack = { navController.popBackStack() },
                    viewModel = hiltViewModel(),
                )
            }
            composable(ROUTE_UPLOAD) {
                UploadScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = hiltViewModel(),
                )
            }
            composable("$ROUTE_PLAYLIST_DETAIL/{$ARG_PLAYLIST_ID}") {
                PlaylistDetailScreen(
                    onTrackClick = { r ->
                        val entity = r.toEntity()
                        nowPlayingTrack = entity
                        entity.streamUrl?.let { url -> playerController.play(entity.id, url) }
                        navController.navigate(ROUTE_PLAYER)
                    },
                    onBack = { navController.popBackStack() },
                    viewModel = hiltViewModel(),
                )
            }
            composable(ROUTE_PLAYER) {
                PlayerScreen(
                    trackTitle = nowPlayingTrack?.title ?: "",
                    trackArtist = nowPlayingTrack?.artist ?: "",
                    onDismiss = { navController.popBackStack() },
                    viewModel = hiltViewModel(),
                )
            }
        }
    }
}
