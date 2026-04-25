package com.serenade.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.serenade.app.core.navigation.AppNavigation
import com.serenade.app.feature.auth.data.AuthRepository
import com.serenade.app.feature.player.PlayerController
import com.serenade.app.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var playerController: PlayerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                AppNavigation(
                    authRepository = authRepository,
                    playerController = playerController,
                )
            }
        }
    }
}
