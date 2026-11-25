package mx.nexum.app.financial.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.nexum.app.auth.ui.EmailVerificationIntent
import mx.nexum.app.auth.ui.EmailVerificationRequestScreen
import mx.nexum.app.auth.ui.EmailVerificationUiState
import mx.nexum.app.ui.theme.NexumTheme

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onIntent: (ProfileIntent) -> Unit,
    modifier: Modifier = Modifier,
    emailVerificationState: EmailVerificationUiState = EmailVerificationUiState(userEmail = state.email, isEmailVerified = state.emailVerified),
    onEmailVerificationIntent: (EmailVerificationIntent) -> Unit = {},
    onNavigateToConfirmEmail: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Ambient Cyber Glow
        Box(
            modifier = Modifier
                .size(350.dp)
                .align(Alignment.TopStart)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PERFIL DE USUARIO DETALLADO",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Metadata card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Detalles de Identidad",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MetadataRow(label = "ID de Usuario", value = state.userId.ifBlank { "Cargando..." })
                    MetadataRow(label = "Verificación de Correo", value = if (state.emailVerified) "Verificado ✓" else "Pendiente de verificación")
                    MetadataRow(label = "Fecha de Alta", value = if (state.createdAt.length >= 10) state.createdAt.take(10) else "—")
                    MetadataRow(label = "Versión del Perfil", value = "v${state.profileVersion}")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Email Verification Card (E010/E011) if not verified
            if (!state.emailVerified) {
                EmailVerificationRequestScreen(
                    state = emailVerificationState.copy(userEmail = state.email, isEmailVerified = state.emailVerified),
                    onIntent = onEmailVerificationIntent,
                    onNavigateToConfirmVerification = onNavigateToConfirmEmail,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Configuración y Preferencias",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.email,
                        onValueChange = {},
                        label = { Text("Correo electrónico (No editable)") },
                        singleLine = true,
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.displayName,
                        onValueChange = { onIntent(ProfileIntent.DisplayNameChanged(it)) },
                        label = { Text("Nombre mostrado") },
                        singleLine = true,
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.language,
                        onValueChange = { onIntent(ProfileIntent.LanguageChanged(it)) },
                        label = { Text("Idioma (ej. es)") },
                        singleLine = true,
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.timezone,
                        onValueChange = { onIntent(ProfileIntent.TimezoneChanged(it)) },
                        label = { Text("Zona horaria (ej. America/Mexico_City)") },
                        singleLine = true,
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    state.errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    state.successMessage?.let { success ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = success, color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { onIntent(ProfileIntent.SubmitUpdate) },
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Guardar Cambios", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfilePreview() {
    NexumTheme {
        ProfileScreen(
            state = ProfileUiState(email = "usuario@nexum.mx", displayName = "Usuario Nexum", userId = "uuid-1234", emailVerified = true, createdAt = "2026-11-20T18:00:00Z"),
            onIntent = {}
        )
    }
}
