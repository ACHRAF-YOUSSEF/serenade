package com.serenade.app.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenade.app.feature.auth.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state: StateFlow<AuthUiState> = _state

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthUiState.Loading
            _state.value = runCatching { repo.login(email, password) }
                .fold(
                    onSuccess = { AuthUiState.Success },
                    onFailure = { error ->
                        if (error is HttpException && error.code() == 403) {
                            AuthUiState.VerificationPending(email = email, expiresAt = null)
                        } else {
                            AuthUiState.Error(error.message ?: "Login failed")
                        }
                    }
                )
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthUiState.Loading
            _state.value = runCatching { repo.register(username, email, password) }
                .fold(
                    onSuccess = { resp -> AuthUiState.VerificationPending(resp.email, resp.expiresAt) },
                    onFailure = { AuthUiState.Error(it.message ?: "Registration failed") }
                )
        }
    }

    fun verifyEmail(email: String, code: String) {
        viewModelScope.launch {
            _state.value = AuthUiState.Loading
            _state.value = runCatching { repo.verifyEmail(email, code) }
                .fold(
                    onSuccess = { AuthUiState.Success },
                    onFailure = { AuthUiState.Error(it.message ?: "Verification failed") }
                )
        }
    }

    fun resendVerification(email: String) {
        viewModelScope.launch {
            _state.value = AuthUiState.Loading
            _state.value = runCatching { repo.resendVerification(email) }
                .fold(
                    onSuccess = { expiresAt -> AuthUiState.VerificationPending(email, expiresAt) },
                    onFailure = { AuthUiState.Error(it.message ?: "Could not resend code") }
                )
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _state.value = AuthUiState.Loading
            _state.value = runCatching { repo.forgotPassword(email) }
                .fold(
                    onSuccess = { AuthUiState.PasswordResetCodeSent(email) },
                    onFailure = { AuthUiState.Error(it.message ?: "Could not send reset code") }
                )
        }
    }

    fun resetPassword(email: String, code: String, newPassword: String) {
        viewModelScope.launch {
            _state.value = AuthUiState.Loading
            _state.value = runCatching { repo.resetPassword(email, code, newPassword) }
                .fold(
                    onSuccess = { AuthUiState.PasswordResetComplete },
                    onFailure = { AuthUiState.Error(it.message ?: "Could not reset password") }
                )
        }
    }

    fun resetState() {
        _state.value = AuthUiState.Idle
    }
}
