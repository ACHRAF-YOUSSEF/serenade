package com.serenade.app.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.serenade.app.feature.auth.data.AuthRepository
import com.serenade.app.feature.auth.presentation.LoginScreen
import com.serenade.app.feature.auth.presentation.RegisterScreen
import com.serenade.app.feature.player.PlayerController
import com.serenade.app.feature.player.presentation.MiniPlayerBar
import com.serenade.app.feature.track.data.entity.TrackEntity
import com.serenade.app.feature.track.presentation.TrackListScreen

private const val ROUTE_LOGIN = "login"
private const val ROUTE_REGISTER = "register"
private const val ROUTE_HOME = "home"

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
                onBarClick = {},
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
        }
    }
}
