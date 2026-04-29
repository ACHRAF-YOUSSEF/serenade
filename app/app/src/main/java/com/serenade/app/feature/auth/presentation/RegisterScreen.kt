package com.serenade.app.feature.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.serenade.app.ui.theme.SerenadeThemeChoice
import com.serenade.app.ui.theme.colorsFor

@Composable
fun RegisterScreen(
    onSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    selectedTheme: SerenadeThemeChoice,
    onThemeSelected: (SerenadeThemeChoice) -> Unit,
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
                if (step == 2) {
                    step = 3
                    viewModel.resetState()
                    return@LaunchedEffect
                }
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
                text = "Step $step / 3",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        LinearProgressIndicator(
            progress = { step / 3f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 28.dp),
        )

        when (step) {
            1 -> RegisterDetailsStep(
                username = username,
                onUsernameChange = { username = it },
                email = email,
                onEmailChange = { email = it },
                password = password,
                onPasswordChange = { password = it },
                passwordVisible = passwordVisible,
                onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
            )
            2 -> VerifyEmailStep(
                email = pendingEmail,
                code = code,
                onCodeChange = { value -> code = value.filter(Char::isDigit).take(5) },
                onResend = { viewModel.resendVerification(pendingEmail) },
                loading = state is AuthUiState.Loading,
            )
            3 -> SetupPreferencesStep(
                selectedTheme = selectedTheme,
                onThemeSelected = onThemeSelected,
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
                } else if (step == 2) {
                    viewModel.verifyEmail(pendingEmail, code)
                } else {
                    onSuccess()
                }
            },
            enabled = state !is AuthUiState.Loading && (step != 2 || code.length == 5),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state is AuthUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else if (step == 2) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Verify account")
            } else if (step == 3) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tune & enter")
            } else {
                Text("Continue")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (step == 3) {
            TextButton(onClick = onSuccess) {
                Text("Skip")
            }
        } else {
            TextButton(onClick = onNavigateToLogin) {
                Text("Already have an account? Log in")
            }
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

    DigitCodeField(
        value = code,
        onValueChange = onCodeChange,
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

@Composable
private fun SetupPreferencesStep(
    selectedTheme: SerenadeThemeChoice,
    onThemeSelected: (SerenadeThemeChoice) -> Unit,
) {
    Text("Tune the room to your taste.", style = MaterialTheme.typography.headlineMedium)
    Text(
        text = "Choose a visual theme now. You can change it later from You.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
    )

    SerenadeThemeChoice.entries.forEach { choice ->
        SignupThemeOption(
            choice = choice,
            selected = selectedTheme == choice,
            onClick = { onThemeSelected(choice) },
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun SignupThemeOption(
    choice: SerenadeThemeChoice,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val preview = colorsFor(choice)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(if (selected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.medium,
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
            listOf(preview.primary, preview.plum, preview.coral).forEach { color ->
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(color)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, androidx.compose.foundation.shape.CircleShape),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(choice.label, style = MaterialTheme.typography.titleSmall)
            Text(choice.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (selected) {
            Text("On", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}
