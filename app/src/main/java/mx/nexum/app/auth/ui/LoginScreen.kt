package mx.nexum.app.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.nexum.app.financial.ui.SkeuomorphicCard
import mx.nexum.app.ui.theme.NexumTheme

@Composable
fun LoginScreen(
    state: LoginUiState,
    onIntent: (LoginIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header / App Title
            Text(
                text = "NEXUM",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "SECURE FINANCIAL OS // HI-FI DECK",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(36.dp))

            // Skeuomorphic Metallic Hi-Fi Container
            SkeuomorphicCard(
                accentColor = MaterialTheme.colorScheme.primary
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "INICIAR SESIÓN",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { onIntent(LoginIntent.EmailChanged(it)) },
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        isError = state.emailError != null,
                        supportingText = state.emailError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        enabled = !state.isLoading,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { onIntent(LoginIntent.PasswordChanged(it)) },
                        label = { Text("Contraseña") },
                        singleLine = true,
                        isError = state.passwordError != null,
                        supportingText = state.passwordError?.let { { Text(it) } },
                        visualTransformation = if (state.isPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            TextButton(onClick = { onIntent(LoginIntent.TogglePasswordVisibility) }) {
                                Text(
                                    text = if (state.isPasswordVisible) "Ocultar" else "Mostrar",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        enabled = !state.isLoading,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    state.attemptsRemaining?.let { remaining ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Intentos restantes: $remaining",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { onIntent(LoginIntent.Submit) },
                        enabled = !state.isLoading,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = "Iniciar Sesión",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { onIntent(LoginIntent.NavigateToForgotPassword) }) {
                        Text(
                            text = "Olvidé mi contraseña",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { onIntent(LoginIntent.NavigateToRegister) }) {
                        Text(
                            text = "¿No tienes cuenta? Regístrate",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    NexumTheme {
        LoginScreen(state = LoginUiState(), onIntent = {})
    }
}
