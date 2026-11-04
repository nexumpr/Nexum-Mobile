package mx.nexum.app.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.nexum.app.auth.data.dto.Frequency
import mx.nexum.app.ui.theme.NexumTheme

@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onIntent: (RegisterIntent) -> Unit,
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
        Text(text = "Crear cuenta", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = { onIntent(RegisterIntent.NameChanged(it)) },
            label = { Text("Nombre completo") },
            singleLine = true,
            isError = state.nameError != null,
            supportingText = state.nameError?.let { { Text(it) } },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = { onIntent(RegisterIntent.EmailChanged(it)) },
            label = { Text("Correo electrónico") },
            singleLine = true,
            isError = state.emailError != null,
            supportingText = state.emailError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = { onIntent(RegisterIntent.PasswordChanged(it)) },
            label = { Text("Contraseña") },
            singleLine = true,
            isError = state.passwordError != null,
            supportingText = state.passwordError?.let { { Text(it) } },
            visualTransformation = passwordTransformation(state.isPasswordVisible),
            trailingIcon = {
                TextButton(onClick = { onIntent(RegisterIntent.TogglePasswordVisibility) }) {
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
            onValueChange = { onIntent(RegisterIntent.ConfirmPasswordChanged(it)) },
            label = { Text("Confirmar contraseña") },
            singleLine = true,
            isError = state.confirmPasswordError != null,
            supportingText = state.confirmPasswordError?.let { { Text(it) } },
            visualTransformation = passwordTransformation(state.isPasswordVisible),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Configuración financiera inicial", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.incomeAmount,
            onValueChange = { onIntent(RegisterIntent.IncomeAmountChanged(it)) },
            label = { Text("Ingreso mensual") },
            singleLine = true,
            isError = state.incomeError != null,
            supportingText = {
                Text(state.incomeError ?: "Frecuencia: mensual")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        state.expenses.forEachIndexed { index, draft ->
            ExpenseRow(
                index = index,
                draft = draft,
                canRemove = state.expenses.size > 1,
                enabled = !state.isLoading,
                onIntent = onIntent
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        state.expensesError?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedButton(
            onClick = { onIntent(RegisterIntent.AddExpense) },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agregar gasto recurrente")
        }

        Spacer(modifier = Modifier.height(20.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = state.termsAccepted,
                onCheckedChange = { onIntent(RegisterIntent.TermsAccepted(it)) },
                enabled = !state.isLoading
            )
            Text(
                text = "Acepto Términos y Condiciones y el aviso de privacidad",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        state.termsError?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { onIntent(RegisterIntent.Submit) },
            enabled = state.termsAccepted && !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Registrarse")
            }
        }
    }
}

@Composable
private fun ExpenseRow(
    index: Int,
    draft: ExpenseDraft,
    canRemove: Boolean,
    enabled: Boolean,
    onIntent: (RegisterIntent) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = draft.concept,
            onValueChange = { onIntent(RegisterIntent.ExpenseChanged(index, draft.copy(concept = it))) },
            label = { Text("Gasto ${index + 1}: concepto") },
            singleLine = true,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = draft.amount,
                onValueChange = { onIntent(RegisterIntent.ExpenseChanged(index, draft.copy(amount = it))) },
                label = { Text("Monto") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = draft.categoryId,
                onValueChange = { onIntent(RegisterIntent.ExpenseChanged(index, draft.copy(categoryId = it))) },
                label = { Text("Categoría") },
                singleLine = true,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )
        }
        if (canRemove) {
            TextButton(
                onClick = { onIntent(RegisterIntent.RemoveExpense(index)) },
                enabled = enabled
            ) {
                Text("Eliminar gasto")
            }
        }
    }
}

private fun passwordTransformation(visible: Boolean): VisualTransformation =
    if (visible) VisualTransformation.None else PasswordVisualTransformation()

@Preview(showBackground = true)
@Composable
private fun RegisterScreenPreview() {
    NexumTheme {
        RegisterScreen(
            state = RegisterUiState(incomeFrequency = Frequency.monthly),
            onIntent = {}
        )
    }
}
