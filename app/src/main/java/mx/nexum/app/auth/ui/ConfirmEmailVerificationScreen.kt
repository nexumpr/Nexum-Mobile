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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.nexum.app.ui.theme.NexumTheme

@Composable
fun ConfirmEmailVerificationScreen(
    state: ConfirmEmailVerificationUiState,
    onIntent: (ConfirmEmailVerificationIntent) -> Unit,
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
            text = "Confirmar verificación de correo",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Pega el código que recibiste por correo. Se respetan mayúsculas y espacios exactos.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = state.token,
            onValueChange = { onIntent(ConfirmEmailVerificationIntent.TokenChanged(it)) },
            label = { Text("Código de verificación") },
            singleLine = true,
            enabled = !state.isLoading && !state.isConfirmed,
            isError = state.isTokenExpiredOrInvalid || state.isTokenAlreadyUsed,
            supportingText = {
                Text(state.errorMessage ?: "El código debe tener al menos 10 caracteres")
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (state.isTokenExpiredOrInvalid || state.isTokenAlreadyUsed) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (state.isTokenAlreadyUsed) {
                            "Este código de verificación ya fue utilizado. Solicita uno nuevo."
                        } else {
                            state.errorMessage ?: "El código de verificación es inválido o ha expirado."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onIntent(ConfirmEmailVerificationIntent.NavigateToRequestVerification) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Solicitar nuevo código")
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (state.isConfirmed) {
            Text(
                text = "Correo electrónico verificado correctamente.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center
            )
            state.requestId?.let { requestId ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Folio de confirmación: $requestId",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Button(
                onClick = { onIntent(ConfirmEmailVerificationIntent.Submit) },
                enabled = state.token.length >= 10 && !state.isLoading,
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
                    Text("Confirmar verificación")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { onIntent(ConfirmEmailVerificationIntent.NavigateToRequestVerification) }) {
                Text("Solicitar un nuevo código")
            }
            TextButton(onClick = { onIntent(ConfirmEmailVerificationIntent.NavigateToLogin) }) {
                Text("Volver a Iniciar Sesión")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfirmEmailVerificationScreenPreview() {
    NexumTheme {
        ConfirmEmailVerificationScreen(
            state = ConfirmEmailVerificationUiState(token = "synthetic_token_123"),
            onIntent = {}
        )
    }
}
