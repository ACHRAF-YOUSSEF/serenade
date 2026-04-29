package com.serenade.app.feature.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun LoginScreen(
    onSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel
) {
    val state by viewModel.state.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var resetCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf(LoginMode.SignIn) }
    var notice by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state) {
        when (state) {
            is AuthUiState.Success -> {
                viewModel.resetState()
                onSuccess()
            }
            is AuthUiState.VerificationPending -> {
                email = (state as AuthUiState.VerificationPending).email
                mode = LoginMode.VerifyEmail
                notice = "Check your email for a fresh verification code."
            }
            is AuthUiState.PasswordResetCodeSent -> {
                email = (state as AuthUiState.PasswordResetCodeSent).email
                mode = LoginMode.ResetPassword
                notice = "Reset code sent. It expires in 10 minutes."
            }
            is AuthUiState.PasswordResetComplete -> {
                mode = LoginMode.SignIn
                notice = "Password updated. Log in with the new password."
                viewModel.resetState()
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
        if (mode != LoginMode.SignIn) {
            IconButton(
                onClick = {
                    mode = LoginMode.SignIn
                    notice = null
                    viewModel.resetState()
                },
                modifier = Modifier.align(Alignment.Start),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }

        Text(
            text = when (mode) {
                LoginMode.SignIn -> "Serenade"
                LoginMode.VerifyEmail -> "Confirm it's you"
                LoginMode.ForgotPassword -> "Reset password"
                LoginMode.ResetPassword -> "Choose a new password"
            },
            style = MaterialTheme.typography.headlineLarge,
        )

        Spacer(modifier = Modifier.height(32.dp))

        when (mode) {
            LoginMode.SignIn -> SignInFields(
                email = email,
                onEmailChange = { email = it },
                password = password,
                onPasswordChange = { password = it },
                passwordVisible = passwordVisible,
                onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
            )
            LoginMode.VerifyEmail -> VerifyLoginFields(
                email = email,
                code = resetCode,
                onCodeChange = { value -> resetCode = value.filter(Char::isDigit).take(5) },
                onResend = { viewModel.resendVerification(email.trim()) },
                loading = state is AuthUiState.Loading,
            )
            LoginMode.ForgotPassword -> ForgotPasswordFields(
                email = email,
                onEmailChange = { email = it },
            )
            LoginMode.ResetPassword -> ResetPasswordFields(
                email = email,
                code = resetCode,
                onCodeChange = { value -> resetCode = value.filter(Char::isDigit).take(5) },
                newPassword = newPassword,
                onNewPasswordChange = { newPassword = it },
                newPasswordVisible = newPasswordVisible,
                onNewPasswordVisibilityChange = { newPasswordVisible = !newPasswordVisible },
            )
        }

        notice?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
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
                when (mode) {
                    LoginMode.SignIn -> viewModel.login(email.trim(), password)
                    LoginMode.VerifyEmail -> viewModel.verifyEmail(email.trim(), resetCode)
                    LoginMode.ForgotPassword -> viewModel.forgotPassword(email.trim())
                    LoginMode.ResetPassword -> viewModel.resetPassword(email.trim(), resetCode, newPassword)
                }
            },
            enabled = state !is AuthUiState.Loading && when (mode) {
                LoginMode.SignIn -> true
                LoginMode.VerifyEmail -> resetCode.length == 5
                LoginMode.ForgotPassword -> email.isNotBlank()
                LoginMode.ResetPassword -> resetCode.length == 5 && newPassword.length >= 8
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state is AuthUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(
                    when (mode) {
                        LoginMode.SignIn -> "Log in"
                        LoginMode.VerifyEmail -> "Verify account"
                        LoginMode.ForgotPassword -> "Send reset code"
                        LoginMode.ResetPassword -> "Update password"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (mode == LoginMode.SignIn) {
            TextButton(onClick = {
                mode = LoginMode.ForgotPassword
                notice = null
                viewModel.resetState()
            }) {
                Text("Forgot password?")
            }

            TextButton(onClick = onNavigateToRegister) {
                Text("Don't have an account? Register")
            }
        }
    }
}

private enum class LoginMode {
    SignIn,
    VerifyEmail,
    ForgotPassword,
    ResetPassword,
}

@Composable
private fun SignInFields(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
) {
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
private fun VerifyLoginFields(
    email: String,
    code: String,
    onCodeChange: (String) -> Unit,
    onResend: () -> Unit,
    loading: Boolean,
) {
    Text(
        text = "Enter the 5 digit code sent to $email.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
    )

    Spacer(modifier = Modifier.height(16.dp))

    DigitCodeField(
        value = code,
        onValueChange = onCodeChange,
        modifier = Modifier.fillMaxWidth(),
    )

    TextButton(onClick = onResend, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
        Text("Resend code")
    }
}

@Composable
private fun ForgotPasswordFields(
    email: String,
    onEmailChange: (String) -> Unit,
) {
    Text(
        text = "We'll send a reset code if that account exists.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ResetPasswordFields(
    email: String,
    code: String,
    onCodeChange: (String) -> Unit,
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    newPasswordVisible: Boolean,
    onNewPasswordVisibilityChange: () -> Unit,
) {
    Text(
        text = "Use the code sent to $email.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
    )

    Spacer(modifier = Modifier.height(16.dp))

    DigitCodeField(
        value = code,
        onValueChange = onCodeChange,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = newPassword,
        onValueChange = onNewPasswordChange,
        label = { Text("New password") },
        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        supportingText = { Text("At least 8 characters.") },
        trailingIcon = {
            IconButton(onClick = onNewPasswordVisibilityChange) {
                Icon(
                    imageVector = if (newPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (newPasswordVisible) "Hide password" else "Show password",
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}
