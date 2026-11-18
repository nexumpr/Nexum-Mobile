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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.nexum.app.financial.data.dto.RecurringPaymentDto
import mx.nexum.app.ui.theme.NexumTheme

@Composable
fun RecurringPaymentsScreen(
    state: RecurringPaymentsUiState,
    onIntent: (RecurringPaymentsIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PAGOS RECURRENTES",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (state.payments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No hay pagos programados.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.payments) { payment ->
                        RecurringPaymentCard(
                            payment = payment,
                            onRecordPayment = { onIntent(RecurringPaymentsIntent.RecordPayment(payment.id, payment.amount)) },
                            onDelete = { onIntent(RecurringPaymentsIntent.DeletePayment(payment.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecurringPaymentCard(
    payment: RecurringPaymentDto,
    onRecordPayment: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = payment.concept,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${payment.type.uppercase()} • ${payment.frequency}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$${payment.amount}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Row {
                // Record Payment (✓)
                TextButton(onClick = onRecordPayment) {
                    Text(text = "✓", color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.titleLarge)
                }
                // Delete / Soft-Delete (×)
                TextButton(onClick = onDelete) {
                    Text(text = "×", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecurringPreview() {
    NexumTheme {
        RecurringPaymentsScreen(
            state = RecurringPaymentsUiState(
                payments = listOf(
                    RecurringPaymentDto("1", "Internet Fibra", 650.0, "cat_1", "fixed", "monthly", 650.0, true),
                    RecurringPaymentDto("2", "Electricidad", 800.0, "cat_2", "variable", "monthly", null, true)
                )
            ),
            onIntent = {}
        )
    }
}
