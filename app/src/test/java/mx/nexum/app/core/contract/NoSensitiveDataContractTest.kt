package mx.nexum.app.core.contract

import com.google.gson.Gson
import mx.nexum.app.core.network.ApiErrorResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NoSensitiveDataContractTest {
    private val projectRoot: File = run {
        var current: File? = File(System.getProperty("user.dir") ?: ".")
        while (current != null && !File(current, "settings.gradle.kts").exists()) {
            current = current.parentFile
        }
        current ?: File(".")
    }

    private val fixtureDir: File
        get() = File(projectRoot, "app/src/test/resources/contract")

    private val secretPatterns = listOf(
        Regex("""\bAKIA[0-9A-Z]{16}\b"""),
        Regex("""-----BEGIN [A-Z ]*PRIVATE KEY-----"""),
        Regex("""(?i)(?:aws_secret_access_key|secret_key|api_secret)\s*[:=]\s*["'][A-Za-z0-9/+=]{30,}["']"""),
        Regex("""(?i)gh[pousr]_[A-Za-z0-9_]{36,255}"""),
        Regex("""\beyJ[A-Za-z0-9_-]{8,}\.[A-Za-z0-9_-]{8,}\.[A-Za-z0-9_-]{8,}\b"""),
        Regex("""(?i)bearer\s+[A-Za-z0-9._-]{12,}""")
    )

    @Test
    fun catalogoSinteticoEstaCompleto() {
        assertTrue("Debe existir el directorio de fixtures sintéticas", fixtureDir.exists())
        val names = fixtureDir.listFiles()
            ?.filter { it.isFile && it.extension == "json" }
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()

        assertEquals(15, names.size)
        assertTrue(names.containsAll(listOf("health_success.json", "session_refresh_success.json", "malformed.json")))
    }

    @Test
    fun fixturesNoContienenSecretosNiDatosPersonalesReales() {
        val violations = mutableListOf<String>()

        fixtureDir.walkTopDown()
            .filter { it.isFile && it.extension == "json" }
            .forEach { file ->
                file.readLines().forEachIndexed { index, line ->
                    secretPatterns.forEach { pattern ->
                        if (pattern.containsMatchIn(line)) {
                            violations.add("${file.name}:${index + 1} -> ${pattern.pattern}")
                        }
                    }
                    if (line.contains("@gmail.com") || line.contains("@hotmail.com") || line.contains("@nexum")) {
                        violations.add("${file.name}:${index + 1} -> dominio personal real")
                    }
                }
            }

        assertEquals(
            "Las fixtures solo deben contener datos sintéticos:\n${violations.joinToString("\n")}",
            0,
            violations.size
        )
    }

    @Test
    fun sobresDeErrorNoFiltranCredencialesEnMensajes() {
        val gson = Gson()
        val errorFiles = fixtureDir.listFiles()
            ?.filter { it.name.startsWith("error_") && it.extension == "json" }
            ?: emptyList()

        assertTrue("Deben existir fixtures de error", errorFiles.isNotEmpty())

        errorFiles.forEach { file ->
            val envelope = gson.fromJson(file.readText(), ApiErrorResponse::class.java)
            val detail = envelope.error
            assertFalse(file.name, detail.message.contains("Bearer"))
            assertFalse(file.name, detail.message.contains("accessToken"))
            assertFalse(file.name, detail.message.contains("refreshToken"))
            assertFalse(file.name, detail.message.contains("@"))
            assertTrue(file.name, detail.traceId?.startsWith("tr_synthetic_") == true)
        }
    }

    @Test
    fun detectoresRechazanFugasSimuladas() {
        val simulatedJwt = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzeW50aGV0aWMifQ.c2lnbmF0dXJl"
        val simulatedAwsKey = "AKIAIOSFODNN7EXAMPLE"

        assertTrue(secretPatterns.any { it.containsMatchIn(simulatedJwt) })
        assertTrue(secretPatterns.any { it.containsMatchIn(simulatedAwsKey) })
    }
}
