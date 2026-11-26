package mx.nexum.app.financial.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.nexum.app.financial.data.dto.VoiceParseResponse

@Composable
fun VoiceAssistantDialog(
    onDismiss: () -> Unit,
    onParseVoice: (String, (VoiceParseResponse?, String?) -> Unit) -> Unit,
    onConfirmMovement: (description: String, amount: String) -> Unit
) {
    var voiceText by remember { mutableStateOf("") }
    var isParsing by remember { mutableStateOf(false) }
    var parseResult by remember { mutableStateOf<VoiceParseResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🎙️ Asistente de Voz Financiero (E075)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Dicta o escribe tu movimiento en español es-MX (ej. 'Registrar gasto de trescientos cincuenta pesos en despensa').",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = voiceText,
                    onValueChange = { voiceText = it; errorMessage = null; parseResult = null },
                    label = { Text("Texto dictado") },
                    singleLine = false,
                    maxLines = 3,
                    enabled = !isParsing,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isParsing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = " Interpretando intención...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                errorMessage?.let { err ->
                    Text(
                        text = "Error: $err",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                parseResult?.let { res ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Intención Parseada:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Acción: ${res.intent}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "Monto extraído: $${res.parsedAmount} ${res.currency}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (parseResult != null) {
                Button(
                    onClick = {
                        val parsed = parseResult
                        if (parsed != null && parsed.parsedAmount.isNotBlank()) {
                            onConfirmMovement(voiceText.trim(), parsed.parsedAmount)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Confirmar Movimiento", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        if (voiceText.isNotBlank()) {
                            isParsing = true
                            onParseVoice(voiceText.trim()) { response, err ->
                                isParsing = false
                                parseResult = response
                                errorMessage = err
                            }
                        }
                    },
                    enabled = voiceText.isNotBlank() && !isParsing
                ) {
                    Text("Interpretar Texto (E075)")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
