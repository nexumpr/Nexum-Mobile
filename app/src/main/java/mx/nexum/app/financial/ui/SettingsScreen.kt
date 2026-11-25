package mx.nexum.app.financial.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.nexum.app.ui.theme.NexumTheme

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier
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
                .align(Alignment.TopEnd)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
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
                text = "CONFIGURACIÓN Y SEGURIDAD",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Theme Switcher Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Apariencia Aero",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (state.isDarkTheme) "Modo Oscuro Cyber" else "Modo Claro Luminoso",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.isDarkTheme,
                        onCheckedChange = { onIntent(SettingsIntent.ToggleTheme(it)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Legal & Help Navigation Cards
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Información y Ayuda",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedButton(
                        onClick = { onIntent(SettingsIntent.OpenPrivacy) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📜 Aviso de Privacidad y Consentimientos")
                    }
                    OutlinedButton(
                        onClick = { onIntent(SettingsIntent.OpenHelp) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("❓ Ayuda, FAQ y Consejos Educativos")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Change Password Card
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
                        text = "Cambiar Contraseña",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.currentPassword,
                        onValueChange = { onIntent(SettingsIntent.CurrentPasswordChanged(it)) },
                        label = { Text("Contraseña actual") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.newPassword,
                        onValueChange = { onIntent(SettingsIntent.NewPasswordChanged(it)) },
                        label = { Text("Nueva contraseña (mín. 8 caracteres)") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = { onIntent(SettingsIntent.ConfirmPasswordChanged(it)) },
                        label = { Text("Confirmar nueva contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
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
                        onClick = { onIntent(SettingsIntent.SubmitChangePassword) },
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
                            Text("Actualizar Contraseña", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Account Actions (Logout, Active Sessions E008/E009 & Delete Account E014) Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Sesión y Cuenta de Usuario",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Active Sessions Button (E008/E009)
                    Button(
                        onClick = { onIntent(SettingsIntent.NavigateToActiveSessions) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("📱 Dispositivos y Sesiones Activas (E008/E009)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
                    }

                    OutlinedButton(
                        onClick = { onIntent(SettingsIntent.Logout) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
                    }

                    // Oleada 2 (E014): Delete Account Button
                    Button(
                        onClick = { onIntent(SettingsIntent.ShowDeleteAccountDialog) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("⚠️ Eliminar Cuenta (E014)", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Oleada 2 (E014): Delete Account Confirmation Dialog
        if (state.showDeleteAccountDialog) {
            AlertDialog(
                onDismissRequest = { onIntent(SettingsIntent.DismissDeleteAccountDialog) },
                title = { Text("⚠️ Eliminar Cuenta Definitivamente", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Esta acción programará la eliminación definitiva de tu cuenta y purgará todas tus sesiones. Escribe exactamente 'DELETE_MY_ACCOUNT' e ingresa tu contraseña actual para confirmar.")
                        OutlinedTextField(
                            value = state.deleteAccountConfirmationText,
                            onValueChange = { onIntent(SettingsIntent.DeleteAccountConfirmationTextChanged(it)) },
                            label = { Text("Confirmación (DELETE_MY_ACCOUNT)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = state.deleteAccountPassword,
                            onValueChange = { onIntent(SettingsIntent.DeleteAccountPasswordChanged(it)) },
                            label = { Text("Contraseña actual") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        state.deleteAccountError?.let { err ->
                            Text(text = err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { onIntent(SettingsIntent.ConfirmDeleteAccount) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Confirmar Borrado (202 Accepted)", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onIntent(SettingsIntent.DismissDeleteAccountDialog) }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsPreview() {
    NexumTheme {
        SettingsScreen(state = SettingsUiState(), onIntent = {})
    }
}
