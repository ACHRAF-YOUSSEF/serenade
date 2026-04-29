package com.serenade.app.feature.auth.presentation

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data object Success : AuthUiState
    data class VerificationPending(val email: String, val expiresAt: String?) : AuthUiState
    data class PasswordResetCodeSent(val email: String) : AuthUiState
    data object PasswordResetComplete : AuthUiState
    data class Error(val message: String) : AuthUiState
}
