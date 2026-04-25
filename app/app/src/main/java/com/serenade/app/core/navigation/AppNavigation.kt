package com.serenade.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.serenade.app.feature.auth.data.AuthRepository
import com.serenade.app.feature.auth.presentation.LoginScreen
import com.serenade.app.feature.auth.presentation.RegisterScreen

private const val ROUTE_LOGIN = "login"
private const val ROUTE_REGISTER = "register"
private const val ROUTE_HOME = "home"

@Composable
fun AppNavigation(authRepository: AuthRepository) {
    val navController = rememberNavController()
    val startDestination = if (authRepository.isLoggedIn()) ROUTE_HOME else ROUTE_LOGIN

    NavHost(navController = navController, startDestination = startDestination) {
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
            HomeScreen()
        }
    }
}

@Composable
private fun HomeScreen() {
    androidx.compose.material3.Text("Home — coming soon")
}
