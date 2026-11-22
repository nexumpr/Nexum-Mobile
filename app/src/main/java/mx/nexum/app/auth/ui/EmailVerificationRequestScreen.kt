package mx.nexum.app.auth.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.nexum.app.ui.theme.NexumTheme

@Composable
fun EmailVerificationRequestScreen(
    state: EmailVerificationUiState,
    onIntent: (EmailVerificationIntent) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToConfirmVerification: () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Verificación de correo",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (state.userEmail.isNotBlank()) {
                Text(
                    text = state.userEmail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            state.feedbackMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (state.isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            when {
                state.isEmailVerified -> {
                    Text(
                        text = "Tu correo electrónico ya está verificado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                else -> {
                    if (state.isAccepted && state.lastRequestId != null) {
                        Text(
                            text = "Folio de solicitud: ${state.lastRequestId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Button(
                        onClick = { onIntent(EmailVerificationIntent.RequestVerification) },
                        enabled = !state.isLoading && state.cooldownSeconds == 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        when {
                            state.isLoading -> CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            state.cooldownSeconds > 0 -> Text("Reenviar en ${state.cooldownSeconds}s")
                            state.isAccepted -> Text("Reenviar correo de verificación")
                            else -> Text("Verificar mi correo")
                        }
                    }
                    TextButton(
                        onClick = onNavigateToConfirmVerification,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ya tengo un código de verificación")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmailVerificationRequestPreview() {
    NexumTheme {
        EmailVerificationRequestScreen(
            state = EmailVerificationUiState(
                userEmail = "usuario@nexum.test",
                cooldownSeconds = 45,
                isAccepted = true,
                lastRequestId = "req-sintetica",
                feedbackMessage = "Solicitud aceptada. Vigencia de 30 minutos."
            ),
            onIntent = {}
        )
    }
}
