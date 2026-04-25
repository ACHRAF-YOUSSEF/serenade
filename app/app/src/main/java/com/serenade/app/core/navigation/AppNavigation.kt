package com.serenade.app.core.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.serenade.app.feature.auth.data.AuthRepository
import com.serenade.app.feature.auth.presentation.LoginScreen
import com.serenade.app.feature.auth.presentation.RegisterScreen
import com.serenade.app.feature.player.PlayerController
import com.serenade.app.feature.player.presentation.MiniPlayerBar
import com.serenade.app.feature.player.presentation.PlayerScreen
import com.serenade.app.feature.track.data.entity.TrackEntity
import com.serenade.app.feature.track.presentation.TrackListScreen

private const val ROUTE_LOGIN = "login"
private const val ROUTE_REGISTER = "register"
private const val ROUTE_HOME = "home"
private const val ROUTE_PLAYER = "player"

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
                )
            }
            composable(ROUTE_HOME) {
                TrackListScreen(
                    onTrackClick = { track ->
                        nowPlayingTrack = track
                        track.streamUrl?.let { url ->
                            playerController.play(track.id, url)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            composable(ROUTE_PLAYER) {
                PlayerScreen(
                    trackTitle = nowPlayingTrack?.title ?: "",
                    trackArtist = nowPlayingTrack?.artist ?: "",
                    onDismiss = { navController.popBackStack() },
                )
            }
        }
    }
}
