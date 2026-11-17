package mx.nexum.app.core.privacy

import androidx.compose.ui.unit.dp
import mx.nexum.app.core.ui.AccessibilityConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PrivacyAndPermissionsTest {

    // --- M070-R01: Solicitud en contexto y degradación elegante / alternativas ---

    @Test
    fun voicePermissionDeniedFallsBackToManualInput() {
        val deniedFallback = PermissionFallbackHandler.resolveVoiceInputFallback(PermissionStatus.DENIED)
        assertEquals(AlternativeInputMode.MANUAL_TEXT_INPUT, deniedFallback)

        val permanentlyDeniedFallback = PermissionFallbackHandler.resolveVoiceInputFallback(PermissionStatus.PERMANENTLY_DENIED)
        assertEquals(AlternativeInputMode.MANUAL_TEXT_INPUT, permanentlyDeniedFallback)

        val notRequestedFallback = PermissionFallbackHandler.resolveVoiceInputFallback(PermissionStatus.NOT_REQUESTED)
        assertEquals(AlternativeInputMode.MANUAL_TEXT_INPUT, notRequestedFallback)

        val grantedFallback = PermissionFallbackHandler.resolveVoiceInputFallback(PermissionStatus.GRANTED)
        assertEquals(AlternativeInputMode.STANDARD_FORM, grantedFallback)
    }

    @Test
    fun applicationCanOperateWithoutSensoryPermissions() {
        // Ningún permiso sensible es bloqueante para el flujo básico
        assertTrue(PermissionFallbackHandler.canOperateWithoutPermission(PermissionStatus.DENIED))
        assertTrue(PermissionFallbackHandler.canOperateWithoutPermission(PermissionStatus.PERMANENTLY_DENIED))
        assertTrue(PermissionFallbackHandler.canOperateWithoutPermission(PermissionStatus.NOT_REQUESTED))
    }

    // --- M070-R02: Legibilidad y accesibilidad Material 3 ---

    @Test
    fun accessibilityMinTouchTargetCompliesWithMaterialGuidelines() {
        assertEquals(48.dp, AccessibilityConstants.MinTouchTargetSize)
    }

    // --- M070-R03: Transporte seguro, política de privacidad y cero filtraciones ---

    @Test
    fun privacyPolicyUrlUsesStrictHttps() {
        val url = PrivacyPolicyConfig.OFFICIAL_PRIVACY_URL
        assertEquals("https://nexumpr.github.io/nexum-privacidad/", url)
        assertTrue(PrivacyPolicyConfig.isUrlSecure(url))
        assertFalse(PrivacyPolicyConfig.isUrlSecure("http://nexumpr.github.io/nexum-privacidad/"))
    }

    @Test
    fun sensitiveTokensAndPasswordsAreRedactedFromLogs() {
        val rawLog = "Error at request with Bearer eyJhbGciOiJIUzI1NiJ9.abc and password=SuperSecret123&refreshToken=rt_xyz987"
        val sanitized = PrivacyPolicyConfig.sanitizeForLogging(rawLog)

        assertFalse(sanitized.contains("eyJhbGciOiJIUzI1NiJ9.abc"))
        assertFalse(sanitized.contains("SuperSecret123"))
        assertFalse(sanitized.contains("rt_xyz987"))
        assertTrue(sanitized.contains("Bearer [REDACTED]"))
        assertTrue(sanitized.contains("password=[REDACTED]"))
        assertTrue(sanitized.contains("refreshToken=[REDACTED]"))
    }

    // --- Auditoría estática de Manifiesto y Network Security ---

    @Test
    fun manifestDoesNotRequestRecordAudioNorPushPrematurely() {
        val manifestFile = File("src/main/AndroidManifest.xml")
        if (manifestFile.exists()) {
            val content = manifestFile.readText()
            assertFalse("El manifiesto no debe contener RECORD_AUDIO", content.contains("RECORD_AUDIO"))
            assertFalse("El manifiesto no debe contener POST_NOTIFICATIONS sin autorización", content.contains("POST_NOTIFICATIONS"))
            assertTrue("El manifiesto debe exigir INTERNET", content.contains("android.permission.INTERNET"))
            assertTrue("El manifiesto debe vincular networkSecurityConfig", content.contains("networkSecurityConfig"))
        }
    }

    @Test
    fun networkSecurityConfigBlocksCleartextAndProtectsPrivacyDomain() {
        val nscFile = File("src/main/res/xml/network_security_config.xml")
        if (nscFile.exists()) {
            val content = nscFile.readText()
            assertTrue("El tráfico en texto claro debe estar deshabilitado en base-config", content.contains("cleartextTrafficPermitted=\"false\""))
            assertTrue("Debe incluir el dominio del aviso de privacidad nexumpr.github.io", content.contains("nexumpr.github.io"))
        }
    }
}
