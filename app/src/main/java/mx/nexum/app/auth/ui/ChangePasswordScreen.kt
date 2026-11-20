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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
fun ChangePasswordScreen(
    state: ChangePasswordUiState,
    onIntent: (ChangePasswordIntent) -> Unit,
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
            text = "Cambiar contraseña",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Ingresa tu contraseña actual y define tu nueva clave de acceso de al menos 12 caracteres.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = state.currentPassword,
            onValueChange = { onIntent(ChangePasswordIntent.CurrentPasswordChanged(it)) },
            label = { Text("Contraseña actual") },
            singleLine = true,
            isError = state.currentPasswordError != null,
            supportingText = state.currentPasswordError?.let { { Text(it) } },
            visualTransformation = if (state.isCurrentPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                TextButton(onClick = { onIntent(ChangePasswordIntent.ToggleCurrentPasswordVisibility) }) {
                    Text(if (state.isCurrentPasswordVisible) "Ocultar" else "Mostrar")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.newPassword,
            onValueChange = { onIntent(ChangePasswordIntent.NewPasswordChanged(it)) },
            label = { Text("Nueva contraseña") },
            singleLine = true,
            isError = state.newPasswordError != null,
            supportingText = { Text(state.newPasswordError ?: "Mínimo 12 caracteres") },
            visualTransformation = if (state.isNewPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                TextButton(onClick = { onIntent(ChangePasswordIntent.ToggleNewPasswordVisibility) }) {
                    Text(if (state.isNewPasswordVisible) "Ocultar" else "Mostrar")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.confirmNewPassword,
            onValueChange = { onIntent(ChangePasswordIntent.ConfirmNewPasswordChanged(it)) },
            label = { Text("Confirmar nueva contraseña") },
            singleLine = true,
            isError = state.confirmNewPasswordError != null,
            supportingText = state.confirmNewPasswordError?.let { { Text(it) } },
            visualTransformation = if (state.isConfirmNewPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                TextButton(onClick = { onIntent(ChangePasswordIntent.ToggleConfirmNewPasswordVisibility) }) {
                    Text(if (state.isConfirmNewPasswordVisible) "Ocultar" else "Mostrar")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onIntent(ChangePasswordIntent.Submit) },
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
                Text("Actualizar contraseña")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { onIntent(ChangePasswordIntent.NavigateBack) }) {
            Text("Cancelar y volver")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChangePasswordScreenPreview() {
    NexumTheme {
        ChangePasswordScreen(
            state = ChangePasswordUiState(),
            onIntent = {}
        )
    }
}
