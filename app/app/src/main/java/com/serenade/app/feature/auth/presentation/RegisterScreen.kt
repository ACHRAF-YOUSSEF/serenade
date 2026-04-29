package com.serenade.app.feature.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun RegisterScreen(
    onSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel,
) {
    val state by viewModel.state.collectAsState()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var step by remember { mutableIntStateOf(1) }
    var passwordVisible by remember { mutableStateOf(false) }
    val pendingEmail = (state as? AuthUiState.VerificationPending)?.email ?: email

    LaunchedEffect(state) {
        when (state) {
            is AuthUiState.Success -> {
                viewModel.resetState()
                onSuccess()
            }
            is AuthUiState.VerificationPending -> {
                step = 2
                email = (state as AuthUiState.VerificationPending).email
            }
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (step == 2) {
                IconButton(onClick = {
                    step = 1
                    viewModel.resetState()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            Text(
                text = "Step $step / 2",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        LinearProgressIndicator(
            progress = { step / 2f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 28.dp),
        )

        if (step == 1) {
            RegisterDetailsStep(
                username = username,
                onUsernameChange = { username = it },
                email = email,
                onEmailChange = { email = it },
                password = password,
                onPasswordChange = { password = it },
                passwordVisible = passwordVisible,
                onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
            )
        } else {
            VerifyEmailStep(
                email = pendingEmail,
                code = code,
                onCodeChange = { value -> code = value.filter(Char::isDigit).take(5) },
                onResend = { viewModel.resendVerification(pendingEmail) },
                loading = state is AuthUiState.Loading,
            )
        }

        if (state is AuthUiState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = (state as AuthUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (step == 1) {
                    viewModel.register(username.trim(), email.trim(), password)
                } else {
                    viewModel.verifyEmail(pendingEmail, code)
                }
            },
            enabled = state !is AuthUiState.Loading && (step == 1 || code.length == 5),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state is AuthUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else if (step == 2) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Verify account")
            } else {
                Text("Continue")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Log in")
        }
    }
}

@Composable
private fun RegisterDetailsStep(
    username: String,
    onUsernameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
) {
    Text("Let's start with a name.", style = MaterialTheme.typography.headlineMedium)
    Text(
        text = "We'll use it on your profile and shared playlists.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 28.dp),
    )

    OutlinedTextField(
        value = username,
        onValueChange = onUsernameChange,
        label = { Text("Display name") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Password") },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        supportingText = { Text("At least 8 characters.") },
        trailingIcon = {
            IconButton(onClick = onPasswordVisibilityChange) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun VerifyEmailStep(
    email: String,
    code: String,
    onCodeChange: (String) -> Unit,
    onResend: () -> Unit,
    loading: Boolean,
) {
    Text("Five digits. Then you're in.", style = MaterialTheme.typography.headlineMedium)
    Text(
        text = "We sent a code to $email. It expires in 10 minutes.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 28.dp),
    )

    OutlinedTextField(
        value = code,
        onValueChange = onCodeChange,
        label = { Text("Verification code") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )

    TextButton(
        onClick = onResend,
        enabled = !loading,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Resend code")
    }
}
