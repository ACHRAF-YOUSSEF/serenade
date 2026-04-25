package com.serenade.app.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenade.app.feature.auth.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
                    onFailure = { AuthUiState.Error(it.message ?: "Login failed") }
                )
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthUiState.Loading
            _state.value = runCatching { repo.register(username, email, password) }
                .fold(
                    onSuccess = { AuthUiState.Success },
                    onFailure = { AuthUiState.Error(it.message ?: "Registration failed") }
                )
        }
    }

    fun resetState() {
        _state.value = AuthUiState.Idle
    }
}
