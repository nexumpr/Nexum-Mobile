package mx.nexum.app.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import mx.nexum.app.core.privacy.PrivacyPolicyConfig

/**
 * Constantes de accesibilidad según las pautas de Google Material 3 y WCAG 2.1 AA.
 * M070-R02: Legibilidad y navegación accesible con tamaño táctil mínimo de 48dp.
 */
object AccessibilityConstants {
    val MinTouchTargetSize = 48.dp
}

/**
 * Botón con garantía de tamaño táctil mínimo (48dp) y descripción semántica explícita.
 */
@Composable
fun AccessibleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescriptionText: String? = null,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .defaultMinSize(
                minWidth = AccessibilityConstants.MinTouchTargetSize,
                minHeight = AccessibilityConstants.MinTouchTargetSize
            )
            .semantics {
                role = Role.Button
                if (!contentDescriptionText.isNullOrBlank()) {
                    contentDescription = contentDescriptionText
                }
            }
    ) {
        content()
    }
}

/**
 * Botón de icono accesible con área táctil mínima de 48dp x 48dp y descripción obligatoria para TalkBack.
 */
@Composable
fun AccessibleIconButton(
    onClick: () -> Unit,
    contentDescriptionText: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable () -> Unit
) {
    require(contentDescriptionText.isNotBlank()) {
        "AccessibleIconButton requiere una contentDescriptionText no vacía para lectores de pantalla."
    }

    Box(
        modifier = modifier
            .defaultMinSize(
                minWidth = AccessibilityConstants.MinTouchTargetSize,
                minHeight = AccessibilityConstants.MinTouchTargetSize
            )
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
                onClickLabel = contentDescriptionText
            )
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = contentDescriptionText
            },
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

/**
 * Componente contextual de explicación de permisos con alternativas no bloqueantes.
 * Satisface M070-R01 (alternativas razonables sin forzar permisos).
 */
@Composable
fun PermissionRationaleCard(
    title: String,
    explanation: String,
    onRequestPermission: () -> Unit,
    onUseFallback: () -> Unit,
    modifier: Modifier = Modifier,
    requestButtonText: String = "Conceder permiso",
    fallbackButtonText: String = "Continuar con entrada manual"
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "$title. $explanation"
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onUseFallback,
                    modifier = Modifier
                        .defaultMinSize(minHeight = AccessibilityConstants.MinTouchTargetSize)
                        .semantics {
                            role = Role.Button
                            contentDescription = fallbackButtonText
                        }
                ) {
                    Text(fallbackButtonText)
                }
                Spacer(modifier = Modifier.size(8.dp))
                AccessibleButton(
                    onClick = onRequestPermission,
                    contentDescriptionText = requestButtonText
                ) {
                    Text(requestButtonText)
                }
            }
        }
    }
}

/**
 * Banner informativo de privacidad accesible con enlace seguro a la política oficial.
 * Satisface M070-R03.
 */
@Composable
fun PrivacyPolicyBanner(
    onOpenPolicy: (url: String) -> Unit,
    modifier: Modifier = Modifier,
    policyUrl: String = PrivacyPolicyConfig.OFFICIAL_PRIVACY_URL
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "Aviso de privacidad de Nexum. Sus datos están protegidos bajo TLS/HTTPS."
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow ?: MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Tus datos se procesan de forma segura y privada.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = { onOpenPolicy(policyUrl) },
                modifier = Modifier
                    .defaultMinSize(minHeight = AccessibilityConstants.MinTouchTargetSize)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Leer aviso de privacidad en nexumpr.github.io"
                    }
            ) {
                Text(
                    text = "Aviso de privacidad",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
