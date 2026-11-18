package mx.nexum.app.core.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * DevSecOps Verification Test Suite — Nexum-Mobile
 *
 * Requisitos rectores:
 * - M090-R01: No deben existir credenciales ni datos sensibles en código o repositorio.
 * - M090-R02: Las advertencias de seguridad/lint deben clasificarse y resolverse o justificarse.
 * - M090-R03: Los riesgos de dependencias y configuraciones deben inventariarse y evaluarse.
 */
class DevSecOpsVerificationTest {

    private val projectRoot: File = run {
        var current: File? = File(System.getProperty("user.dir") ?: ".")
        while (current != null && !File(current, "settings.gradle.kts").exists()) {
            current = current.parentFile
        }
        current ?: File(".")
    }

    // Reglas regex de patrones de secretos conocidos de alta entropía
    private val secretPatterns = listOf(
        Regex("""\bAKIA[0-9A-Z]{16}\b"""), // AWS Access Key ID
        Regex("""-----BEGIN [A-Z ]*PRIVATE KEY-----"""), // PEM Private Key
        Regex("""(?i)(?:aws_secret_access_key|secret_key|api_secret)\s*[:=]\s*["'][A-Za-z0-9/+=]{30,}["']"""), // Claves secretas de APIs
        Regex("""(?i)gh[pousr]_[A-Za-z0-9_]{36,255}""") // GitHub Tokens
    )

    // Permisos sensibles estrictamente prohibidos sin especificación funcional explícita
    private val unauthorizedPermissions = listOf(
        "android.permission.RECORD_AUDIO",
        "android.permission.POST_NOTIFICATIONS",
        "android.permission.READ_CONTACTS",
        "android.permission.WRITE_CONTACTS",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.CAMERA",
        "android.permission.READ_SMS",
        "android.permission.SEND_SMS",
        "android.permission.READ_CALL_LOG"
    )

    @Test
    fun manifestContainsOnlyAuthorizedPermissions() {
        val manifestFile = File(projectRoot, "app/src/main/AndroidManifest.xml")
        assertTrue("El archivo AndroidManifest.xml debe existir", manifestFile.exists())
        val content = manifestFile.readText()

        // 1. Debe incluir permiso obligatorio de internet
        assertTrue(
            "El manifiesto debe incluir android.permission.INTERNET",
            content.contains("android.permission.INTERNET")
        )

        // 2. No debe incluir ningún permiso invasivo o sensible no autorizado
        unauthorizedPermissions.forEach { permission ->
            assertFalse(
                "El manifiesto contiene un permiso no autorizado por la constitución: $permission",
                content.contains(permission)
            )
        }
    }

    @Test
    fun networkSecurityConfigStrictlyProhibitsCleartextTraffic() {
        val networkConfigFile = File(projectRoot, "app/src/main/res/xml/network_security_config.xml")
        assertTrue("El archivo network_security_config.xml debe existir", networkConfigFile.exists())
        val content = networkConfigFile.readText()

        // Base config debe prohibir tráfico en texto claro
        assertTrue(
            "network_security_config.xml debe forzar cleartextTrafficPermitted=\"false\"",
            content.contains("""cleartextTrafficPermitted="false"""")
        )

        // No debe existir ninguna excepción con cleartextTrafficPermitted="true"
        assertFalse(
            "network_security_config.xml no debe permitir texto claro bajo ninguna circunstancia",
            content.contains("""cleartextTrafficPermitted="true"""")
        )

        // Dominio de política de privacidad verificado
        assertTrue(
            "El dominio nexumpr.github.io debe estar declarado explícitamente en network_security_config",
            content.contains("nexumpr.github.io")
        )
    }

    @Test
    fun sourceCodeTreeHasZeroHardcodedSecrets() {
        val sourceDir = File(projectRoot, "app/src/main")
        assertTrue("El directorio de fuentes principales debe existir", sourceDir.exists())

        val violations = mutableListOf<String>()

        sourceDir.walkTopDown()
            .filter { it.isFile && (it.extension == "kt" || it.extension == "xml" || it.extension == "json") }
            .forEach { file ->
                val lines = file.readLines()
                lines.forEachIndexed { index, line ->
                    secretPatterns.forEach { pattern ->
                        if (pattern.containsMatchIn(line)) {
                            violations.add("${file.relativeTo(projectRoot).path}:${index + 1} coincide con ${pattern.pattern}")
                        }
                    }
                }
            }

        assertEquals(
            "Se detectaron posibles secretos hardcodeados en el código fuente:\n${violations.joinToString("\n")}",
            0,
            violations.size
        )
    }

    @Test
    fun negativeTest_SecretScannerRejectsSimulatedAwsKey() {
        val simulatedLeak = "val awsKey = \"AKIAIOSFODNN7EXAMPLE\""
        val matched = secretPatterns.any { it.containsMatchIn(simulatedLeak) }
        assertTrue("El escáner de secretos debe detectar una clave AWS simulada", matched)
    }

    @Test
    fun negativeTest_SecretScannerRejectsSimulatedPrivateKeyHeader() {
        val simulatedPem = "-----BEGIN RSA PRIVATE KEY-----\nMIIEowIBAAKCAQEA0..."
        val matched = secretPatterns.any { it.containsMatchIn(simulatedPem) }
        assertTrue("El escáner de secretos debe detectar cabeceras de claves privadas PEM", matched)
    }

    @Test
    fun negativeTest_AuditRejectsUnauthorizedPermissionsInSyntheticManifest() {
        val syntheticManifest = """
            <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                <uses-permission android:name="android.permission.INTERNET" />
                <uses-permission android:name="android.permission.RECORD_AUDIO" />
            </manifest>
        """.trimIndent()

        val foundUnauthorized = unauthorizedPermissions.filter { syntheticManifest.contains(it) }
        assertEquals(
            "El auditor de manifiestos debe detectar permisos no autorizados como RECORD_AUDIO",
            listOf("android.permission.RECORD_AUDIO"),
            foundUnauthorized
        )
    }

    @Test
    fun negativeTest_NetworkConfigAuditRejectsInsecureCleartext() {
        val syntheticInsecureConfig = """
            <network-security-config>
                <base-config cleartextTrafficPermitted="true" />
            </network-security-config>
        """.trimIndent()

        val permitsCleartext = syntheticInsecureConfig.contains("""cleartextTrafficPermitted="true"""")
        assertTrue(
            "El auditor de seguridad de red debe marcar como insegura cualquier configuración con cleartext permitido",
            permitsCleartext
        )
    }

    @Test
    fun lintConfigurationEnforcesSecurityRulesAsFatal() {
        val lintXml = File(projectRoot, "app/lint.xml")
        assertTrue("El archivo app/lint.xml debe existir para gobernar Android Lint", lintXml.exists())
        val content = lintXml.readText()

        val requiredFatalRules = listOf(
            "BadHostnameVerifier",
            "NetworkSecurityConfig",
            "HardcodedDebugMode",
            "InsecureBaseConfiguration",
            "TrustAllX509TrustManager",
            "ExportedContentProvider",
            "ExportedReceiver",
            "UnsafeProtectedBroadcastReceiver",
            "AuthLeak"
        )

        requiredFatalRules.forEach { rule ->
            assertTrue(
                "La regla $rule debe estar configurada como fatal en lint.xml",
                content.contains("""id="$rule" severity="fatal"""")
            )
        }
    }
}
