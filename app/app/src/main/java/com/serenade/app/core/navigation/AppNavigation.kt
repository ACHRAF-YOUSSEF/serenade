package com.serenade.app.core.navigation

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.serenade.app.core.database.Genre
import com.serenade.app.feature.auth.data.AuthRepository
import com.serenade.app.feature.auth.presentation.LoginScreen
import com.serenade.app.feature.auth.presentation.RegisterScreen
import com.serenade.app.feature.download.presentation.DownloadScreen
import com.serenade.app.feature.player.PlaybackItem
import com.serenade.app.feature.player.PlayerController
import com.serenade.app.feature.player.presentation.MiniPlayerBar
import com.serenade.app.feature.player.presentation.PlayerScreen
import com.serenade.app.feature.playlist.presentation.LibraryScreen
import com.serenade.app.feature.playlist.presentation.PlaylistDetailScreen
import com.serenade.app.feature.search.presentation.SearchScreen
import com.serenade.app.feature.splash.presentation.SplashScreen
import com.serenade.app.feature.track.data.stableArtworkUrl
import com.serenade.app.feature.track.data.entity.TrackEntity
import com.serenade.app.feature.track.data.remote.dto.TrackResponse
import com.serenade.app.feature.track.presentation.TrackListScreen
import com.serenade.app.feature.upload.presentation.UploadScreen
import com.serenade.app.feature.you.presentation.YouScreen
import com.serenade.app.ui.theme.*
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
    artworkUrl = stableArtworkUrl(id, artworkUrl),
    localPath = null,
    streamUrl = streamUrl,
    streamUrlExpiresAt = null,
    isDownloaded = false,
    providerId = "serenade",
    updatedAt = Instant.now(),
)

private fun TrackEntity.playbackUri(): String? =
    localPath?.let { Uri.fromFile(File(it)).toString() } ?: streamUrl

private const val ROUTE_LOGIN    = "login"
private const val ROUTE_REGISTER = "register"
private const val ROUTE_SPLASH   = "splash"
private const val ROUTE_HOME     = "home"
private const val ROUTE_SEARCH   = "search"
private const val ROUTE_LIBRARY  = "library"
private const val ROUTE_DOWNLOADS = "downloads"
private const val ROUTE_UPLOAD   = "upload"
private const val ROUTE_YOU      = "you"
private const val ROUTE_PLAYER   = "player"
private const val ROUTE_PLAYLIST_DETAIL = "playlist"
private const val ARG_PLAYLIST_ID = "playlistId"

// Bottom tabs visible post-login
private data class NavTab(
    val route: String,
    val label: String,
    val iconSelected: ImageVector,
    val iconDefault: ImageVector,
)

private val TABS = listOf(
    NavTab(ROUTE_HOME,    "Listen",  Icons.Filled.Home,        Icons.Outlined.Home),
    NavTab(ROUTE_SEARCH,  "Search",  Icons.Filled.Search,      Icons.Outlined.Search),
    NavTab(ROUTE_LIBRARY, "Library", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
    NavTab(ROUTE_UPLOAD,  "Studio",  Icons.Filled.CloudUpload,  Icons.Outlined.CloudUpload),
    NavTab(ROUTE_YOU,     "You",     Icons.Filled.Person,       Icons.Outlined.Person),
)

private val TAB_ROUTES = TABS.map { it.route }.toSet()

@Composable
fun AppNavigation(
    authRepository: AuthRepository,
    playerController: PlayerController,
    selectedTheme: SerenadeThemeChoice,
    onThemeSelected: (SerenadeThemeChoice) -> Unit,
) {
    val navController = rememberNavController()
    val postSplashDestination = if (authRepository.isLoggedIn()) ROUTE_HOME else ROUTE_LOGIN
    val playbackState by playerController.state.collectAsState()

    var playbackQueue by remember { mutableStateOf<List<TrackEntity>>(emptyList()) }
    val nowPlayingTrack = remember(playbackQueue, playbackState.currentTrackId) {
        playbackQueue.firstOrNull { it.id == playbackState.currentTrackId }
    }

    fun playQueue(tracks: List<TrackEntity>, selected: TrackEntity): Boolean {
        val entries = tracks.mapNotNull { track ->
            track.playbackUri()?.let { url ->
                track to PlaybackItem(
                    trackId = track.id,
                    streamUrl = url,
                    title = track.title,
                    artist = track.artist,
                    album = track.album,
                    durationMs = track.durationMs,
                    artworkUrl = track.artworkUrl,
                )
            }
        }
        val startIndex = entries.indexOfFirst { it.first.id == selected.id }
        if (startIndex < 0) return false
        playbackQueue = entries.map { it.first }
        playerController.playQueue(entries.map { it.second }, startIndex)
        return true
    }

    fun playResponseQueue(tracks: List<TrackResponse>, selected: TrackResponse): Boolean {
        val entities = tracks.map { it.toEntity() }
        val selectedEntity = entities.firstOrNull { it.id == selected.id } ?: return false
        return playQueue(entities, selectedEntity)
    }

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = currentRoute in TAB_ROUTES

    Scaffold(
        containerColor = SrBg,
        bottomBar = {
            if (showBottomBar) {
                Column(
                    modifier = Modifier
                        .background(Color.Transparent)
                        .navigationBarsPadding(),
                ) {
                    MiniPlayerBar(
                        state = playbackState,
                        trackTitle = nowPlayingTrack?.title ?: playbackState.currentTitle,
                        trackArtist = nowPlayingTrack?.artist ?: playbackState.currentArtist,
                        artworkUrl = nowPlayingTrack?.artworkUrl ?: playbackState.currentArtworkUrl,
                        onTogglePlayPause = playerController::togglePlayPause,
                        onBarClick = {
                            if (playbackState.currentTrackId != null) {
                                navController.navigate(ROUTE_PLAYER)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    SrBottomNav(
                        navController = navController,
                        currentRoute = currentRoute,
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_SPLASH,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(ROUTE_SPLASH) {
                SplashScreen(
                    onFinished = {
                        navController.navigate(postSplashDestination) {
                            popUpTo(ROUTE_SPLASH) { inclusive = true }
                        }
                    },
                )
            }
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
                    onTrackClick = { track, allTracks -> playQueue(allTracks, track) },
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
                    onTrackClick = { track, queue ->
                        if (playResponseQueue(queue, track)) navController.navigate(ROUTE_PLAYER)
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
                        if (playQueue(listOf(track), track)) navController.navigate(ROUTE_PLAYER)
                    },
                    onBack = { navController.popBackStack() },
                    viewModel = hiltViewModel(),
                )
            }
            composable(ROUTE_YOU) {
                YouScreen(
                    selectedTheme = selectedTheme,
                    onThemeSelected = onThemeSelected,
                    onDownloadsClick = { navController.navigate(ROUTE_DOWNLOADS) },
                    onLogout = {
                        authRepository.logout()
                        navController.navigate(ROUTE_LOGIN) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
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
                    onTrackClick = { track, queue ->
                        if (playResponseQueue(queue, track)) navController.navigate(ROUTE_PLAYER)
                    },
                    onBack = { navController.popBackStack() },
                    viewModel = hiltViewModel(),
                )
            }
            composable(ROUTE_PLAYER) {
                PlayerScreen(
                    trackTitle = nowPlayingTrack?.title ?: playbackState.currentTitle ?: "",
                    trackArtist = nowPlayingTrack?.artist ?: playbackState.currentArtist ?: "",
                    trackDurationMs = nowPlayingTrack?.durationMs ?: playbackState.durationMs,
                    artworkUrl = nowPlayingTrack?.artworkUrl ?: playbackState.currentArtworkUrl,
                    onDismiss = { navController.popBackStack() },
                    viewModel = hiltViewModel(),
                )
            }
        }
    }
}

@Composable
private fun SrBottomNav(
    navController: NavController,
    currentRoute: String?,
) {
    Box(
        modifier = Modifier.background(
            Brush.verticalGradient(
                listOf(Color.Transparent, SrBgDeep.copy(alpha = 0.86f), SrBgDeep)
            )
        )
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
        ) {
            TABS.forEach { tab ->
                val selected = currentRoute == tab.route
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        if (!selected) {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (selected) tab.iconSelected else tab.iconDefault,
                            contentDescription = tab.label,
                            modifier = Modifier.size(22.dp),
                        )
                    },
                    label = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                            ),
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SrPrimary,
                        selectedTextColor = SrPrimary,
                        indicatorColor = SrSurfaceHi,
                        unselectedIconColor = SrTextMute,
                        unselectedTextColor = SrTextMute,
                    ),
                )
            }
        }
    }
}
