package mx.nexum.app.auth.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.nexum.app.ui.theme.NexumTheme

@Composable
fun ResetPasswordScreen(
    state: ResetPasswordUiState,
    onIntent: (ResetPasswordIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Restablecer contraseña",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(12.dp))

        val instructions = if (state.userEmail != null) {
            "Ingresa el código enviado a ${state.userEmail} y define tu nueva contraseña."
        } else {
            "Ingresa el código que recibiste por correo y define tu nueva contraseña."
        }

        Text(
            text = instructions,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = state.token,
            onValueChange = { onIntent(ResetPasswordIntent.TokenChanged(it)) },
            label = { Text("Código de recuperación") },
            singleLine = true,
            isError = state.tokenError != null,
            supportingText = { Text(state.tokenError ?: "Vigencia de 30 minutos") },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (state.isTokenExpiredOrInvalid) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = state.tokenError ?: "El código de recuperación es inválido o ha expirado.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onIntent(ResetPasswordIntent.NavigateToForgotPassword) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Solicitar nuevo código")
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = state.newPassword,
            onValueChange = { onIntent(ResetPasswordIntent.PasswordChanged(it)) },
            label = { Text("Nueva contraseña") },
            singleLine = true,
            isError = state.passwordError != null,
            supportingText = state.passwordError?.let { { Text(it) } },
            visualTransformation = if (state.isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                TextButton(onClick = { onIntent(ResetPasswordIntent.TogglePasswordVisibility) }) {
                    Text(if (state.isPasswordVisible) "Ocultar" else "Mostrar")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.confirmPassword,
            onValueChange = { onIntent(ResetPasswordIntent.ConfirmPasswordChanged(it)) },
            label = { Text("Confirmar nueva contraseña") },
            singleLine = true,
            isError = state.confirmPasswordError != null,
            supportingText = state.confirmPasswordError?.let { { Text(it) } },
            visualTransformation = if (state.isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onIntent(ResetPasswordIntent.Submit) },
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Restablecer contraseña")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { onIntent(ResetPasswordIntent.NavigateToForgotPassword) }) {
            Text("¿No tienes código o venció? Solicitar nuevo")
        }
        TextButton(onClick = { onIntent(ResetPasswordIntent.NavigateToLogin) }) {
            Text("Volver a Iniciar Sesión")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResetPasswordScreenPreview() {
    NexumTheme {
        ResetPasswordScreen(
            state = ResetPasswordUiState(userEmail = "usuario@nexum.test"),
            onIntent = {}
        )
    }
}
