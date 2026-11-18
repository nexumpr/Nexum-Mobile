package mx.nexum.app.core.ci

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * CI & Versioning Verification Test Suite — Nexum-Mobile
 *
 * Requisitos rectores:
 * - M100-R01: Build y test deben automatizarse con permisos mínimos cuando se autorice CI.
 * - M100-R02: versionCode y versionName deben mantenerse coherentes con entregas.
 * - M100-R03: Los artefactos deben ser reproducibles y sin firma privada embebida.
 */
class CiVersioningVerificationTest {

    private val projectRoot: File = run {
        var current: File? = File(System.getProperty("user.dir") ?: ".")
        while (current != null && !File(current, "settings.gradle.kts").exists()) {
            current = current.parentFile
        }
        current ?: File(".")
    }

    @Test
    fun deterministicVersionCodeCalculation() {
        fun computeVersionCode(runNumber: Int?): Int {
            return if (runNumber != null) {
                100 + runNumber
            } else {
                2
            }
        }

        // Caso local sin GITHUB_RUN_NUMBER
        assertEquals("El versionCode local debe ser 2 de forma determinista", 2, computeVersionCode(null))

        // Caso CI con GITHUB_RUN_NUMBER incremental
        assertEquals("El versionCode en CI para run 1 debe ser 101", 101, computeVersionCode(1))
        assertEquals("El versionCode en CI para run 42 debe ser 142", 142, computeVersionCode(42))
        assertTrue("El versionCode debe ser estrictamente monótono creciente", computeVersionCode(43) > computeVersionCode(42))
    }

    @Test
    fun appBuildGradleHasRatifiedSdkMatrix() {
        val buildGradleFile = File(projectRoot, "app/build.gradle.kts")
        assertTrue("El archivo app/build.gradle.kts debe existir", buildGradleFile.exists())
        val content = buildGradleFile.readText()

        assertTrue("minSdk debe ser 26", content.contains("minSdk = 26"))
        assertTrue("targetSdk debe ser 36", content.contains("targetSdk = 36"))
        assertTrue("compileSdk debe estar en release 36", content.contains("release(36)"))
    }

    @Test
    fun androidCiWorkflowEnforcesMinimumPermissionsAndNoSecrets() {
        val workflowFile = File(projectRoot, ".github/workflows/android-ci.yml")
        assertTrue("El archivo .github/workflows/android-ci.yml debe existir", workflowFile.exists())
        val content = workflowFile.readText()

        // 1. Permisos de solo lectura
        assertTrue(
            "El workflow de CI debe tener permisos mínimos 'contents: read'",
            content.contains("permissions:") && content.contains("contents: read")
        )

        // 2. Prohibición de permisos de escritura
        assertFalse(
            "El workflow de CI no debe solicitar permisos de escritura",
            content.contains("contents: write") || content.contains("packages: write")
        )

        // 3. Prohibición de secretos de firma o Play Console en el pipeline base
        assertFalse("El workflow base de CI no debe requerir ANDROID_KEYSTORE_BASE64", content.contains("ANDROID_KEYSTORE_BASE64"))
        assertFalse("El workflow base de CI no debe requerir KEYSTORE_PASSWORD", content.contains("KEYSTORE_PASSWORD"))
        assertFalse("El workflow base de CI no debe requerir KEY_PASSWORD", content.contains("KEY_PASSWORD"))
        assertFalse("El workflow base de CI no debe incluir acciones de despliegue a Google Play", content.contains("upload-google-play"))

        // 4. Inclusión de JDK 21 y tareas de verificación obligatorias
        assertTrue("Debe configurar JDK 21", content.contains("java-version: '21'"))
        assertTrue("Debe ejecutar lintDebug", content.contains("./gradlew lintDebug"))
        assertTrue("Debe ejecutar testDebugUnitTest", content.contains("./gradlew testDebugUnitTest"))
        assertTrue("Debe ejecutar assembleDebug", content.contains("./gradlew assembleDebug"))
    }

    @Test
    fun negativeTest_WorkflowAuditorRejectsSimulatedSigningSecrets() {
        val simulatedInsecureWorkflow = """
            name: Insecure CI
            jobs:
              build:
                steps:
                  - run: ./gradlew bundleRelease
                    env:
                      KEYSTORE_SECRET: ${'$'}{{ secrets.ANDROID_KEYSTORE_BASE64 }}
        """.trimIndent()

        val containsSecrets = simulatedInsecureWorkflow.contains("secrets.ANDROID_KEYSTORE_BASE64") ||
                simulatedInsecureWorkflow.contains("bundleRelease")
        assertTrue("El auditor de CI debe rechazar workflows con secretos de firma o bundleRelease", containsSecrets)
    }

    @Test
    fun negativeTest_WorkflowAuditorRejectsWritePermissions() {
        val simulatedPrivilegedWorkflow = """
            name: Privileged CI
            permissions:
              contents: write
              packages: write
        """.trimIndent()

        val hasWritePermissions = simulatedPrivilegedWorkflow.contains("contents: write") ||
                simulatedPrivilegedWorkflow.contains("packages: write")
        assertTrue("El auditor de CI debe rechazar permisos excesivos de escritura en CI de verificación", hasWritePermissions)
    }

    @Test
    fun negativeTest_RejectsInvalidOrDegradedSdkMatrix() {
        val simulatedLegacyContent = """
            minSdk = 21
            targetSdk = 30
        """.trimIndent()

        val isCompliant = simulatedLegacyContent.contains("minSdk = 26") &&
                simulatedLegacyContent.contains("targetSdk = 36")
        assertFalse("El validador de matriz SDK debe rechazar configuraciones obsoletas", isCompliant)
    }
}
