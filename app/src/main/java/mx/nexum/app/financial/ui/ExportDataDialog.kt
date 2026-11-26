package mx.nexum.app.financial.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.nexum.app.financial.data.dto.UserExportResponse

@Composable
fun ExportDataDialog(
    onDismiss: () -> Unit,
    exportData: UserExportResponse?,
    isLoading: Boolean,
    errorMessage: String?
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "📥 Volcado Completo de Datos (E020)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Copia de respaldo en formato JSON de tu cuenta, perfil, movimientos, metas y consentimientos registrados.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                when {
                    isLoading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Generando volcado de datos desde AWS...", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    errorMessage != null -> {
                        Text(
                            text = "Error: $errorMessage",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    exportData != null -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = "Fecha de exportación: ${exportData.exportedAt.take(19)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                Text(text = "ID Usuario: ${exportData.profile.id ?: "—"}", style = MaterialTheme.typography.bodySmall)
                                Text(text = "Movimientos exportados: ${exportData.movements.size}", style = MaterialTheme.typography.bodySmall)
                                Text(text = "Metas exportadas: ${exportData.goals.size}", style = MaterialTheme.typography.bodySmall)
                                Text(text = "Consentimientos exportados: ${exportData.consents.size}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
