package com.serenade.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.serenade.app.core.preferences.ThemePreferenceStore
import com.serenade.app.core.navigation.AppNavigation
import com.serenade.app.feature.auth.data.AuthRepository
import com.serenade.app.feature.player.PlayerController
import com.serenade.app.ui.theme.AppTheme
import com.serenade.app.ui.theme.SerenadeThemeChoice
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var playerController: PlayerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()
        enableEdgeToEdge()
        setContent {
            val themeStore = remember { ThemePreferenceStore(applicationContext) }
            val selectedTheme by themeStore.selectedTheme.collectAsState(initial = SerenadeThemeChoice.Midnight)
            val scope = rememberCoroutineScope()
            AppTheme(themeChoice = selectedTheme) {
                AppNavigation(
                    authRepository = authRepository,
                    playerController = playerController,
                    selectedTheme = selectedTheme,
                    onThemeSelected = { choice ->
                        scope.launch { themeStore.setTheme(choice) }
                    },
                )
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0,
            )
        }
    }
}
